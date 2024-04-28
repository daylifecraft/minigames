package com.daylifecraft.minigames.minigames.instances.games.towerdefence

import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.instance.InstancePlayerState
import com.daylifecraft.minigames.instance.instances.games.MiniGameWorldInstance
import com.daylifecraft.minigames.minigames.instances.AbstractMiniGameInstance
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.gui.PlaceTowerGui
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.gui.TowerManagementGui
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.monsters.MonsterData
import com.daylifecraft.minigames.minigames.items.MiniGameItem
import com.daylifecraft.minigames.minigames.profile.RoundProfile
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import com.daylifecraft.minigames.text.i18n.TranslateText
import com.daylifecraft.minigames.util.RoundIterator
import com.google.gson.JsonObject
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.event.entity.EntityAttackEvent
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerPreEatEvent
import net.minestom.server.event.player.PlayerSwapItemEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.event.trait.CancellableEvent
import net.minestom.server.event.trait.InventoryEvent
import net.minestom.server.item.Material
import net.minestom.server.scoreboard.Sidebar
import java.util.UUID

class TowerDefenceInstance private constructor(
  craftInstance: MiniGameWorldInstance,
  roundProfile: RoundProfile,
  playerSettings: Map<Player, JsonObject>,
  gameFilters: JsonObject,
  private val playersDistribution: MutableMap<UUID, TowerDefenceTeamInfo>,
  private val playersSpawnPositions: MutableMap<UUID, TowerDefenceTeamInfo.PlayerPosition>,
) : AbstractMiniGameInstance(craftInstance, roundProfile, playerSettings, gameFilters) {

  private val towersData: MutableList<TowerData> = mutableListOf()
  val towerDefenceEconomy: TowerDefenceEconomy = TowerDefenceEconomy()
  private val instanceTicker: TowerDefenceInstanceTicker = TowerDefenceInstanceTicker(this)
  private val playersSidebars: Map<Player, Sidebar> =
    getRoundPlayerSettings().keys.associateWith {
      Sidebar(Component.empty())
    }

  val waveController: TowerDefenceWaveController = TowerDefenceWaveController(this)

  private val monstersQueue: Map<TowerDefenceTeamInfo, MutableList<MonsterData>> =
    playersDistribution.values.toSet().associateWith { mutableListOf() }

  private var currentWaveMonstersQueue: MutableMap<TowerDefenceTeamInfo, MutableList<MonsterData>> = mutableMapOf()
  private var currentWaveSpawnByTickCount = 1

  private val teamsMonsterTargetsIterators: Map<TowerDefenceTeamInfo, RoundIterator<Player>> =
    playersDistribution.values.associateWith {
      RoundIterator(
        playersDistribution.filterValues { value -> value == it }
          .keys
          .map { PlayerManager.getPlayerByUuid(it) }
          .requireNoNulls(),
      )
    }

  private val playersHealthMap: Map<Player, PlayerHealth> = playerSettings.keys.associateWith {
    PlayerHealth(ON_STARTUP_PLAYER_HEALTH)
  }

  override fun onAllPlayersReady() {
    super.onAllPlayersReady()

    startRound()
    playersSidebars.forEach { (player, sidebar) -> sidebar.addViewer(player) }
  }

  override fun startRound() {
    super.startRound()

    instanceTicker.run()
  }

  private fun initEvents() {
    // Block events
    addEventListener<PlayerBlockBreakEvent>(PlayerBlockBreakEvent::cancel)
    addEventListener<PlayerBlockInteractEvent>(PlayerBlockInteractEvent::cancel)

    addEventListener<PlayerSwapItemEvent>(PlayerSwapItemEvent::cancel)
    addEventListener<ItemDropEvent>(ItemDropEvent::cancel)
    addEventListener<PlayerPreEatEvent>(PlayerPreEatEvent::cancel)

    addEventListener<InventoryPreClickEvent>(::onInventoryEvent)

    addEventListener<PlayerUseItemEvent>(::onPlayerUseItemEvent)

    // Damage entity event
    addEventListener<EntityAttackEvent>(::onPlayerDamageEntity)
  }

  private fun onPlayerUseItemEvent(event: PlayerUseItemEvent) {
    event.isCancelled = true
    onPlayerItemUse(event.player, event.itemStack)
  }

  private fun onInventoryEvent(event: InventoryEvent) {
    if (event is InventoryPreClickEvent) {
      event.isCancelled = true
    }
  }

  override fun setupPlayerToInstance(player: Player, instancePlayerState: InstancePlayerState) {
    instancePlayerState.gameMode = GameMode.ADVENTURE
  }

  override fun onPlayerJoinToInstance(player: Player) {
    if (getSpectatorsList().contains(player)) {
      return
    }

    player.isAllowFlying = true
    player.isFlying = true

    player.refreshCommands()

    addMiniGameItem(
      player,
      0,
      MiniGameItem(
        material = Material.PUFFERFISH_BUCKET,
        amount = 1,
        text = TranslateText("games.towerdefence.items.towerbuilder.name"),
        lore = TranslateText("games.towerdefence.items.towerbuilder.description"),
        onInteract = { _, interactedPlayer -> openManagementGui(interactedPlayer) },
      ),
    )
    addMiniGameItem(
      player,
      1,
      MiniGameItem(
        material = Material.CHEST,
        amount = 1,
        text = TranslateText("games.towerdefence.items.shop.name"),
        lore = TranslateText("games.towerdefence.items.shop.description"),
      ),
    )
    addMiniGameItem(
      player,
      2,
      MiniGameItem(
        material = Material.CHORUS_FRUIT,
        amount = 1,
        text = TranslateText("games.towerdefence.items.monstersender.name"),
        lore = TranslateText("games.towerdefence.items.monstersender.description"),
      ),
    )

    addMiniGameItem(
      player,
      8,
      MiniGameItem(
        material = Material.ENDER_PEARL,
        amount = 1,
        text = TranslateText("games.towerdefence.items.return.name"),
        lore = TranslateText("games.towerdefence.items.return.description"),
        onInteract = { _, interactedPlayer -> interactedPlayer.teleport(miniGameWorldInstance.getSpawnPos(interactedPlayer)) },
        cooldownSeconds = 3,
      ),
    )
  }

  override fun onPlayerLeaveFromInstance(player: Player) {
    val playerInventory = player.inventory

    playerInventory.clear()

    player.isFlying = false
    player.isAllowFlying = false

    player.refreshCommands()
  }

  fun findTowerData(blockPosition: Point): TowerData? {
    for (towerData in towersData) {
      if (towerData.isContainsPosition(blockPosition)) return towerData
    }

    return null
  }

  fun removeTower(towerData: TowerData, clearBlocks: Boolean = false) {
    towersData.remove(towerData)

    if (clearBlocks) {
      towerData.remove(miniGameWorldInstance.instance)
    }
  }

  fun addTower(towerData: TowerData) {
    towersData.add(towerData)
  }

  fun replaceTowerByNew(oldTowerData: TowerData, newTowerData: TowerData) {
    require(oldTowerData in towersData) {
      throw IllegalArgumentException("Trying to replace non-existence tower data")
    }

    // Copy old start position
    val originalPosition = oldTowerData.position
    removeTower(oldTowerData)
    val newTower = newTowerData.createCopy(originalPosition, oldTowerData.ownerData)

    // Add tower and build
    addTower(newTower)
    newTower.deepCopyTower(
      newTowerData,
      miniGameWorldInstance.instance,
      TowerDefenseManager.get().defaultTowerDefenseInstance,
      oldTowerData.towerHeight,
    )
  }

  fun updateHud() {
    for ((player, sidebar) in playersSidebars) {
      val lines = PlayerLanguage.get(player).string(
        "games.towerdefence.main-hud",
        "currentWave" to waveController.currentWave.toString(),
        "hp" to getPlayerHealth(player).toString(),
        "gold" to towerDefenceEconomy.getBalance(player).toString(),
      ).split("\n").reversed()

      if (sidebar.lines.isNotEmpty()) {
        lines.forEachIndexed { index: Int, line: String ->
          sidebar.updateLineContent(index.toString(), MiniMessage.miniMessage().deserialize(line))
        }
      } else {
        lines.forEachIndexed { index: Int, line: String ->
          sidebar.createLine(Sidebar.ScoreboardLine(index.toString(), MiniMessage.miniMessage().deserialize(line), index))
        }
      }
    }
  }

  private fun openManagementGui(player: Player) {
    // Try to open Management GUI
    val targetBlock = player.getTargetBlockPosition(MAX_TARGET_AT_BLOCK_DISTANCE)

    if (targetBlock != null) {
      val towerDataAtTargetBlock = findTowerData(targetBlock)

      if (towerDataAtTargetBlock != null && towerDataAtTargetBlock.ownerData?.second == getTeamInfo(player.uuid)) {
        TowerManagementGui.showTowerManagementGui(player, this, towerDataAtTargetBlock)
        return
      }
    }

    // Or else open place tower GUI
    if (getOptimalPlacePoint(player) == null) {
      PlayerLanguage.get(player).sendMiniMessage("games.towerdefence.builder.fail.no-space")
      return
    }

    PlaceTowerGui.showPlaceTowerGui(player, this)
  }

  fun onPlayerTryPlaceTower(player: Player, towerData: TowerData) {
    val optimalPlacePoint = getOptimalPlacePoint(player, towerData)
    val playerLanguage = PlayerLanguage.get(player)

    if (optimalPlacePoint == null) {
      playerLanguage.sendMiniMessage("games.towerdefence.builder.fail.no-space")
      return
    }

    if (!towerDefenceEconomy.hasOnBalance(player, towerData.cost)) {
      // TODO i18n key
      return
    }

    val newTower = towerData.createCopy(optimalPlacePoint)

    // Add tower and build
    addTower(newTower)
    newTower.deepCopyTower(towerData, miniGameWorldInstance.instance, TowerDefenseManager.get().defaultTowerDefenseInstance)

    // Change tower owner
    newTower.ownerData = Pair(player.uuid, getTeamInfo(player.uuid)!!)

    // Withdraw balance
    towerDefenceEconomy.addBalance(player, -towerData.cost)
  }

  private fun getOptimalPlacePoint(player: Player, towerData: TowerData? = null): Point? {
    val targetBlock = player.getTargetBlockPosition(MAX_TARGET_AT_BLOCK_DISTANCE)?.add(0.0, 1.0, 0.0) ?: return null
    val allowedBlockType = getTeamInfo(player.uuid)!!.blockType

    return if (towerData != null) {
      towerData.findEmptyPlacePositionFrom(allowedBlockType, miniGameWorldInstance.instance, targetBlock)
    } else {
      TowerDefenseManager.get().getLoadedTowersData().first()
        .findEmptyPlacePositionFrom(allowedBlockType, miniGameWorldInstance.instance, targetBlock)
    }
  }

  fun addMonstersToQueue(
    owner: Player,
    teamInfo: TowerDefenceTeamInfo,
    monsterData: MonsterData,
    amount: Int = 1,
  ) {
    require(teamInfo in monstersQueue) {
      throw IllegalArgumentException("Trying to add monster to non-existing team info")
    }

    repeat(amount) {
      val newMonsterData = monsterData.deepCopyWithOwner(owner)
      monstersQueue[teamInfo]?.add(newMonsterData)
    }
  }

  fun removeMonstersFromQueue(teamInfo: TowerDefenceTeamInfo, monsterData: MonsterData, amount: Int = -1) {
    if (amount == -1) {
      // Clear all monsters
      monstersQueue[teamInfo]?.removeIf { it.isSimilar(monsterData) }
    } else {
      // Clear specified amount
      repeat(amount) { _ ->
        val founded = monstersQueue[teamInfo]?.find { it.isSimilar(monsterData) }
        if (founded == null) {
          return
        }

        monstersQueue[teamInfo]?.remove(founded)
      }
    }
  }

  fun onWaveUpdated(ticksPerWave: Int) {
    currentWaveMonstersQueue = mutableMapOf()
    monstersQueue.forEach { (key, value) -> currentWaveMonstersQueue[key] = value.toMutableList() }

    // Recalculate how many monsters per tick we will spawn
    val maxMonstersByTeam = currentWaveMonstersQueue.values.maxOfOrNull { it.count() } ?: 1

    currentWaveSpawnByTickCount = (maxMonstersByTeam / ticksPerWave.toDouble()).toInt() + 1
  }

  fun doSpawnMonstersPerTick(currentTicksCount: Long) {
    if (currentTicksCount % 5 != 0L) {
      return
    }

    for (entry in currentWaveMonstersQueue) {
      val monstersData = entry.value.take(currentWaveSpawnByTickCount)

      if (monstersData.isEmpty()) return

      // Spawn monsters
      for (monsterData in monstersData) {
        val targetPlayer = teamsMonsterTargetsIterators[entry.key]?.nextNotNullElement()

        if (targetPlayer == null) {
          // Skip monster initialization
          // TODO May be throw exception?
          continue
        }

        val entity = monsterData.getLivingEntity(miniGameWorldInstance.instance)
        val randomPoint = Pos(entry.key.mobSpawnZone.getRandomPoint())

        entity.teleport(randomPoint)

        // TODO Calculate optimal minimal distance
//        val bb: BoundingBox = entity.boundingBox
//        val minimalDistance = sqrt(bb.width() * bb.width() + bb.depth() * bb.depth()) / 2.0;

        entity.target = targetPlayer
        entity.navigator.setPathTo(
          playersSpawnPositions[targetPlayer.uuid]?.towerZone?.getRandomPoint(),
          MINIMAL_PATH_COMPLETE_DISTANCE,
          MonsterData.PATH_FINDING_LIMIT,
          MonsterData.PATH_FINDING_LIMIT,
        ) {
          onMonsterReachedDestinationPoint(monsterData, targetPlayer, entity)
        }
      }

      entry.value.removeAll(monstersData)
    }
  }

  /**
   * TODO Remove this method when core pathfinder will be fixed
   */
  fun clearStuckedMonsters() {
    for (monsterData in getAllMonsterDataQueue()) {
      monsterData.linkedEntityCreatures
        .filter { it.target is Player }
        .forEach { entityCreature ->
          if (!entityCreature.navigator.isComplete &&
            entityCreature.position.distance(entityCreature.navigator.pathPosition)
            <= MINIMAL_PATH_COMPLETE_DISTANCE + ERROR_PATH_DISTANCE
          ) {
            onMonsterReachedDestinationPoint(monsterData, entityCreature.target as Player, entityCreature)
          }
        }
    }
  }

  private fun onMonsterReachedDestinationPoint(
    monsterData: MonsterData,
    targetPlayer: Player,
    entityCreature: EntityCreature,
  ) {
    // Damage player & remove entity
    val result = playersHealthMap[targetPlayer]?.doDamage(monsterData.damageAmount)

    monsterData.killLinkedEntity(entityCreature)

    onPlayerHealthUpdate(targetPlayer, result)
  }

  private fun onPlayerDamageEntity(event: EntityAttackEvent) {
    if (event.entity.entityType != EntityType.PLAYER) {
      return
    }
    if (event.target !is EntityCreature) {
      return
    }
    val entityCreature = event.target as EntityCreature
    val linkedMonsterData = getMonsterDataByLinkedEntity(entityCreature) ?: return

    linkedMonsterData.killLinkedEntity(entityCreature)
  }

  fun doIncomeDistribution(currentTicksCount: Long) {
    // Update income only per second
    if (currentTicksCount % 20 != 0L) {
      return
    }

    // TODO Разобраться
    getAllMonsterDataQueue()
      .filter { it.hasLinkToAnyEntity() }
      .forEach {
        if (it.ownerPlayer != null) {
          towerDefenceEconomy.addBalance(it.ownerPlayer!!, it.incomeAmount)
        }
      }
  }

  fun doTowersAttack(currentTicksCount: Long) {
    for (towerData in towersData.toMutableList()) {
      if (towerData.attackSpeedTicks == null) continue
      if (towerData.attackDamage == null) continue
      if (towerData.attackSpeedTicks != 0 && currentTicksCount % towerData.attackSpeedTicks.toLong() != 0L) continue

      val nearbyMonster = miniGameWorldInstance.instance
        .getNearbyEntities(towerData.position, towerData.attackRange.toDouble())
        .filterIsInstance<EntityCreature>()
        .maxByOrNull {
          it.getNavigatorIndex() ?: -1
        }

      if(nearbyMonster == null) continue

      nearbyMonster.damage(DamageType.PLAYER_ATTACK, towerData.attackDamage.toFloat())
    }
  }

  private fun getMonsterDataByLinkedEntity(entityCreature: EntityCreature): MonsterData? = getAllMonsterDataQueue().find { it.hasLinkToEntity(entityCreature) }

  private fun getAllMonsterDataQueue(): List<MonsterData> = monstersQueue.values.flatten()

  fun getTeamInfo(playerUuid: UUID): TowerDefenceTeamInfo? = playersDistribution[playerUuid]

  private fun getPlayerHealth(player: Player): Int = playersHealthMap[player]?.getHealth() ?: -1

  private fun onPlayerHealthUpdate(player: Player, isPlayerDead: Boolean? = null) {
    if ((isPlayerDead != null && isPlayerDead) || (getPlayerHealth(player) <= 0)) {
      // TODO handle player death
    }
  }

  public override fun stopRound() {
    super.stopRound()

    instanceTicker.stop()
    playersSidebars.forEach { (player, sidebar) -> sidebar.removeViewer(player) }
  }

  override fun removePlayerFromRound(player: Player) {
    super.removePlayerFromRound(player)

    playersDistribution.remove(player.uuid)
    playersSpawnPositions.remove(player.uuid)
  }

  companion object {
    private const val MAX_TARGET_AT_BLOCK_DISTANCE = 7

    private const val ON_STARTUP_PLAYER_HEALTH = 100

    private const val MINIMAL_PATH_COMPLETE_DISTANCE = 1.0

    private const val ERROR_PATH_DISTANCE = 0.6

    /**
     * Creates Active MiniGame instance
     * @param craftInstance World instance
     * @param roundProfile RoundProfile (database object)
     * @param playerSettings Player with their settings
     * @param gameFilters final game filters
     * @return created game instance
     */
    fun createInstance(
      craftInstance: MiniGameWorldInstance,
      roundProfile: RoundProfile,
      playerSettings: Map<Player, JsonObject>,
      gameFilters: JsonObject,
      playersDistribution: Map<UUID, TowerDefenceTeamInfo>,
      playerSpawnPositions: Map<UUID, TowerDefenceTeamInfo.PlayerPosition>,
    ): TowerDefenceInstance {
      val instance = TowerDefenceInstance(
        craftInstance,
        roundProfile,
        playerSettings,
        gameFilters,
        playersDistribution.toMutableMap(),
        playerSpawnPositions.toMutableMap(),
      )

      instance.initEvents()
      instance.registerEvents()

      return instance
    }
  }
}

private fun CancellableEvent.cancel() {
  isCancelled = true
}

private fun EntityCreature.getNavigatorIndex(): Int? {
  if(navigator.isComplete) {
    return null
  }

  val pathField = navigator.javaClass.getDeclaredField("path")
  pathField.isAccessible = true

  val pathObject = pathField[navigator]
  val indexField = pathObject.javaClass.getDeclaredField("index")
  indexField.isAccessible = true

  return indexField[pathObject] as Int
}
