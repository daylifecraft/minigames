package com.daylifecraft.minigames.minigames.instances.games.towerdefence.monsters

import net.minestom.server.attribute.Attribute
import net.minestom.server.attribute.AttributeModifier
import net.minestom.server.attribute.AttributeOperation
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.EquipmentSlot
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.villager.VillagerMeta
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.math.max

class MonsterData private constructor(
  val monsterId: String,
  val entityType: EntityType,
  val items: Map<EquipmentSlot, Material>,
  val nbtTags: Map<String, Any?>,
  val passengerType: EntityType?,
  val healthAmount: Int,
  val speed: Double,
  val damageAmount: Int,
  val cost: Int,
  val incomeAmount: Int,
  var ownerPlayer: Player? = null,
) {
  var linkedEntityCreature: EntityCreature? = null

  override fun toString(): String = "MonsterData(name='$monsterId', entityType=$entityType, items=$items, nbtTags=$nbtTags, passengerType=$passengerType, healthAmount=$healthAmount, speed=$speed, damageAmount=$damageAmount, cost=$cost, incomeAmount=$incomeAmount)"

  fun getLivingEntity(spawningInstance: Instance): EntityCreature {
    val entityCreature = EntityCreature(entityType)

    entityCreature.setInstance(spawningInstance)

    // Edit entity health
    entityCreature.getAttribute(Attribute.MAX_HEALTH).baseValue = healthAmount.toFloat()
    entityCreature.health = healthAmount.toFloat()

    // Edit entity equipment
    for (equipmentItem in items) {
      entityCreature.setEquipment(
        equipmentItem.key,
        ItemStack.builder(equipmentItem.value).build(),
      )
    }

    if (entityType == EntityType.VILLAGER && "VillagerData" in nbtTags) {
      // TODO VillagerData Type & level
      val profession = (nbtTags["VillagerData"] as Map<*, *>)["Profession"] as String

      (entityCreature.livingEntityMeta as VillagerMeta).villagerData =
        VillagerMeta.VillagerData(
          VillagerMeta.Type.PLAINS,
          VillagerMeta.Profession.valueOf(profession.uppercase()),
          VillagerMeta.Level.NOVICE,
        )
    }

    // Add passenger
    if (passengerType != null) {
      val passenger = EntityCreature(passengerType)
      passenger.setInstance(spawningInstance)
      entityCreature.addPassenger(passenger)
    }

    // Edit Speed
    val resultModifier = max(0.0, speed) - 1
    entityCreature.getAttribute(Attribute.MOVEMENT_SPEED)
      .addModifier(
        AttributeModifier(
          WALK_ATTRIBUTE_MODIFIER_UUID,
          WALK_ATTRIBUTE_MODIFIER_NAME,
          resultModifier,
          AttributeOperation.MULTIPLY_TOTAL,
        ),
      )
    entityCreature.getAttribute(Attribute.FLYING_SPEED)
      .addModifier(
        AttributeModifier(
          FLY_ATTRIBUTE_MODIFIER_UUID,
          FLY_ATTRIBUTE_MODIFIER_NAME,
          resultModifier,
          AttributeOperation.MULTIPLY_TOTAL,
        ),
      )

    linkedEntityCreature = entityCreature

    return entityCreature
  }

  fun deepCopyWithOwner(player: Player): MonsterData = MonsterData(
    monsterId,
    entityType,
    items,
    nbtTags,
    passengerType,
    healthAmount,
    speed,
    damageAmount,
    cost,
    incomeAmount,
    player,
  )

  fun isSimilar(monsterData: MonsterData): Boolean = monsterId == monsterData.monsterId

  fun getNameKey(): String = "games.towerdefence.monsters.%s.name".format(monsterId)

  fun getDescriptionKey(): String = "games.towerdefence.monsters.%s.description".format(monsterId)

  companion object {
    const val PATH_FINDING_LIMIT = 2500.0

    const val WALK_ATTRIBUTE_MODIFIER_NAME: String = "TOWER_DEFENCE_WALK_SPEED_MODIFIER"
    const val FLY_ATTRIBUTE_MODIFIER_NAME: String = "TOWER_DEFENCE_FLY_SPEED_MODIFIER"

    private val WALK_ATTRIBUTE_MODIFIER_UUID: UUID = UUID.nameUUIDFromBytes(WALK_ATTRIBUTE_MODIFIER_NAME.toByteArray(StandardCharsets.UTF_8))

    private val FLY_ATTRIBUTE_MODIFIER_UUID: UUID = UUID.nameUUIDFromBytes(FLY_ATTRIBUTE_MODIFIER_NAME.toByteArray(StandardCharsets.UTF_8))

    private fun getItems(inputMap: Map<*, *>?): Map<EquipmentSlot, Material> {
      if (inputMap == null) {
        return emptyMap()
      }

      return inputMap.mapKeys { EquipmentSlot.valueOf(it.key as String) }.mapValues {
        Material.fromNamespaceId((it.value as String).lowercase())!!
      }
    }

    private fun getFromConfigSection(
      monsterData: Map<String, Any?>,
    ): MonsterData {
      val passengerNamespaceId = (monsterData["passenger"] as String?)?.lowercase() ?: ""
      val nbtTags = monsterData["nbt"]

      return MonsterData(
        monsterId = monsterData["name"] as String,
        entityType = EntityType.fromNamespaceId(
          (monsterData["model"] as String).lowercase(),
        ),
        items = getItems(monsterData["items"] as Map<*, *>?),
        nbtTags = if (nbtTags != null) {
          (nbtTags as Map<*, *>).mapKeys { it.key as String }
        } else {
          emptyMap()
        },
        passengerType = EntityType.fromNamespaceId(passengerNamespaceId),
        healthAmount = (monsterData["hp"] as Number).toInt(),
        speed = (monsterData["speed"] as Number).toDouble(),
        damageAmount = (monsterData["damage"] as Number).toInt(),
        cost = (monsterData["cost"] as Number).toInt(),
        incomeAmount = (monsterData["income"] as Number).toInt(),
      )
    }

    /**
     * Function parse monsters from MiniGame config
     * @param monsterConfigSection Monster config section (
     * @return list of loaded monsters
     */
    fun parseMonstersList(monstersData: List<Map<String, Any?>>): List<MonsterData> {
      val resultList: MutableList<MonsterData> = mutableListOf()

      monstersData.forEach { monsterData ->
        resultList.add(
          getFromConfigSection(
            monsterData,
          ),
        )
      }

      return resultList
    }
  }
}
