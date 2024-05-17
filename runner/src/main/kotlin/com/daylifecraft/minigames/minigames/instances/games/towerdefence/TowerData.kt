package com.daylifecraft.minigames.minigames.instances.games.towerdefence

import com.daylifecraft.common.config.ConfigFile
import com.daylifecraft.common.text.PlayerText
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.common.hologram.Hologram
import com.daylifecraft.common.hologram.HologramManager
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.monsters.MonsterData
import com.daylifecraft.minigames.text.i18n.TranslateText
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

class TowerData private constructor(
  /**
   * Tower ID
   */
  val towerId: String,

  /**
   * Tower Level
   */
  var level: Int,

  /**
   * Item for render in gui
   */
  var guiItem: Material?,

  /**
   * Tower position on map
   */
  val position: Point,

  /**
   * Tower upgrade path
   */
  val upgradePath: TowerUpgradePath?,

  /**
   * DisplayName i18n key
   */
  val displayNameKey: String,

  /**
   * Description i18n key
   */
  val descriptionKey: String,

  /**
   * Cost for a tower place
   */
  val cost: Int,

  /**
   * Delay between shots in ticks
   */
  val attackSpeedTicks: Int?,

  /**
   * Damage per one shot
   */
  val attackDamage: Int?,

  /**
   * Distance between tower and entity in blocks
   */
  val attackRange: Int,

  /**
   * Tower size in blocks. (Example: if towerSize == 3 it means that tower is 3x3)
   */
  private val towerSize: Int,

  var towerHeight: Int = MAXIMUM_TOWER_HEIGHT,

  var ownerData: Pair<UUID, TowerDefenceTeamInfo>? = null,

  val uniqueTowerId: Int = getUniqueTowerId()
) {
  private var hologram: Hologram? = null
  var targetEntityId: Int? = null
    private set
  private var lastTargetEntityId: Int? = null
  private var nextAttackMillis: Long? = null

  /**
   * Returns all positions of current tower
   * @param instance World instance
   */
  private fun getTowerBlocks(): List<Point> = iterateThrowTowerLayers().asSequence().toList()

  fun deepCopyTower(towerData: TowerData, toInstance: Instance, fromInstance: Instance, lastTowerHeight: Int = towerHeight) {
    val layerOffset: Int = (towerSize - 1) / 2

    // Iterate throw tower layers
    for (layer in 0..max(lastTowerHeight, towerHeight)) {
      // Iterate throw blocks in layer

      for (xOffset in -layerOffset..layerOffset) {
        for (zOffset in -layerOffset..layerOffset) {
          toInstance.setBlock(
            position.add(xOffset.toDouble(), layer.toDouble(), zOffset.toDouble()),
            fromInstance.getBlock(towerData.position.add(xOffset.toDouble(), layer.toDouble(), zOffset.toDouble())),
          )
        }
      }
    }
  }

  fun iterateThrowTowerLayers() = iterator {
    // Iterate throw tower layers
    for (layer in 0..towerHeight) {
      yieldAll(iterateThrowTowerLayer(layer))
    }
  }

  private fun iterateThrowTowerLayer(layer: Int) = iterator<Point> {
    val layerOffset: Int = (towerSize - 1) / 2

    for (xOffset in -layerOffset..layerOffset) {
      for (zOffset in -layerOffset..layerOffset) {
        yield(
          Pos(
            position.add(xOffset.toDouble(), layer.toDouble(), zOffset.toDouble()),
          ),
        )
      }
    }
  }

  private fun iterateThrowPlatformBlocks() = iterator<Point> {
    val layerOffset: Int = (towerSize - 1) / 2

    for (xOffset in -layerOffset..layerOffset) {
      for (zOffset in -layerOffset..layerOffset) {
        yield(
          Pos(
            position.add(xOffset.toDouble(), -1.0, zOffset.toDouble()),
          ),
        )
      }
    }
  }

  fun isContainsPosition(position: Point): Boolean = getTowerBlocks().any { it.samePoint(position) }

  /**
   * Returns True if towerId and towerLevel is equals
   * Otherwise False
   */
  fun isEqual(towerId: String, towerLevel: Int): Boolean = this.towerId == towerId && this.level == towerLevel

  fun createCopy(newPosition: Point, newOwnerData: Pair<UUID, TowerDefenceTeamInfo>? = null,
                 uniqueTowerId: Int = getUniqueTowerId()
  ): TowerData = TowerData(
    towerId,
    level,
    guiItem,
    newPosition,
    upgradePath,
    displayNameKey,
    descriptionKey,
    cost,
    attackSpeedTicks,
    attackDamage,
    attackRange,
    towerSize,
    towerHeight,
    newOwnerData,
    uniqueTowerId
  )

  fun calculateTowerHeight(instance: Instance) {
    var result = 1

    for (layer in 1..MAXIMUM_TOWER_HEIGHT) {
      if (!iterateThrowTowerLayer(layer).asSequence().map { instance.getBlock(it).isAir }.contains(false)) {
        break
      }

      result++
    }

    towerHeight = result
  }

  fun remove(worldInstance: Instance) {
    for (point in iterateThrowTowerLayers()) {
      worldInstance.setBlock(point, Block.AIR)
    }

    hologram?.remove()
    hologram = null
  }

  fun findEmptyPlacePositionFrom(allowedBlockType: Material?, instance: Instance, startPoint: Point): Point? {
    if (isEnoughSpaceFrom(allowedBlockType, instance, startPoint)) {
      return startPoint
    }

    for (xOffset in -1..1) {
      for (zOffset in -1..1) {
        if (xOffset == 0 && zOffset == 0) {
          continue
        }

        val currentPoint = startPoint.add(xOffset.toDouble(), 0.0, zOffset.toDouble())
        if (isEnoughSpaceFrom(allowedBlockType, instance, currentPoint)) {
          return currentPoint
        }
      }
    }

    return null
  }

  private fun isEnoughSpaceFrom(allowedBlockType: Material?, instance: Instance, startPoint: Point): Boolean {
    val towerData = createCopy(startPoint)

    for (point in towerData.iterateThrowTowerLayers()) {
      if (!instance.getBlock(point).isAir) return false
    }

    for (point in towerData.iterateThrowPlatformBlocks()) {
      if (instance.getBlock(point).registry().material() != allowedBlockType) return false
    }

    return true
  }

  fun getSellPrice(): Int = (cost * (TowerDefenseManager.get().towerCostBuybackPercent / 100.0)).toInt()

  fun generateHologram(viewers: List<Player> = emptyList()) {
    hologram = HologramManager.createHologram(
      Pos(position).add(0.0, towerHeight - 2.0, 0.0), getHologramText(), viewers
    )
  }

  fun addHologramViewer(player: Player) {
    try {
      hologram?.addViewer(player)
    }catch (exception: IllegalArgumentException) {
      // Noting to do
    }
  }

  fun removeHologramViewer(player: Player) {
    try {
      hologram?.removeViewer(player)
    }catch (exception: IllegalArgumentException) {
      // Noting to do
    }
  }

  fun updateHologram() {
    hologram?.updateText(getHologramText())
    hologram?.doRender()
  }


  fun removeHologram() {
    hologram?.remove()
  }

  private fun getHologramText(): PlayerText {
    return TranslateText(
      "debug.td.debughud.tower",
      "towerDisplayName" to TranslateText(displayNameKey),
      "towerSpawnID" to uniqueTowerId.toString(),
      "towerLevel" to level.toString(),
      "towerAttackRange" to attackRange.toString(),
      "towerDamage" to attackDamage.toString(),
      "towerTarget" to targetEntityId.toString(),
      "towerLastTarget" to lastTargetEntityId.toString(),
      "towerNextAttack" to nextAttackMillis.toString(),
      "towerOwner" to if(ownerData != null) PlayerManager.getPlayerByUuid(ownerData!!.first)?.username else "null"
    )
  }

  fun updateTargetEntity(entityCreature: EntityCreature) {
    val entityId = entityCreature.entityId

    if(targetEntityId == null) {
      targetEntityId = entityId
      lastTargetEntityId = entityId
    }else if(targetEntityId != entityId) {
      lastTargetEntityId = targetEntityId
      targetEntityId = entityId
    }
  }

  fun canAttackInCurrentTick(currentTickCount: Long): Boolean {
    if(attackDamage == null || attackSpeedTicks == null) return false
    val canAttack = attackSpeedTicks == 0 || currentTickCount % attackSpeedTicks.toLong() != 0L

    if(canAttack) {
      nextAttackMillis = 0
    }else {
      nextAttackMillis = (attackSpeedTicks.toLong() - currentTickCount % attackSpeedTicks.toLong()) * 50
    }

    return canAttack
  }

  companion object {
    private var lastUniqueTowerId = AtomicInteger(0)
    private const val DISPLAY_NAME_PATH = "games.towerdefence.towers.%s.displayname"
    private const val DESCRIPTION_PATH = "games.towerdefence.towers.%s.description"
    private const val MAXIMUM_TOWER_HEIGHT = 255

    private fun getFromConfigurationSection(towerSize: Int, configSection: ConfigFile): TowerData {
      val towerId = configSection.getString("tower")!!
      val towerLevel = configSection.getInt("level")!!
      return TowerData(
        towerId = towerId,
        level = towerLevel,
        guiItem = Material.fromNamespaceId(configSection.getString("item")!!.lowercase()),
        position = Pos(configSection.getInt("x")!!.toDouble(), configSection.getInt("y")!!.toDouble(), configSection.getInt("z")!!.toDouble()),
        getTowerUpgradePath(configSection, towerId, towerLevel),
        DISPLAY_NAME_PATH.format(towerId),
        DESCRIPTION_PATH.format(towerId),
        cost = configSection.getInt("cost")!!,
        configSection.getInt("cast_speed"),
        configSection.getInt("damage"),
        configSection.getInt("attack_range")!!,
        towerSize = towerSize,
      )
    }

    private fun getTowerUpgradePath(configSection: ConfigFile, towerId: String, towerLevel: Int): TowerUpgradePath? = if ("upgrade_path" in configSection.getKeys("")) {
      TowerUpgradePath.getByConfigSection(
        configSection.getValueList("upgrade_path"),
        towerId,
        towerLevel,
      )
    } else {
      null
    }

    private fun getUniqueTowerId(): Int {
      return lastUniqueTowerId.incrementAndGet()
    }

    /**
     * Function parse towers from MiniGame config
     * @param towerConfigSection Towers config section (
     * @return list of loaded towers
     */
    fun parseTowersList(towerConfigSection: ConfigFile): List<TowerData> {
      val towerSize = towerConfigSection.getInt("towersize")!!
      val resultList: MutableList<TowerData> = mutableListOf()

      towerConfigSection.getTypedList<Any>("towers")!!.forEach { towerSection ->
        if (towerSection is Map<*, *>) {
          resultList.add(
            getFromConfigurationSection(
              towerSize,
              ConfigFile(towerSection.mapKeys { it.key as String }.mapValues { it.value!! }),
            ),
          )
        }
      }

      return resultList
    }
  }
}
