package com.daylifecraft.minigames.hologram

import com.daylifecraft.common.text.PlayerText
import net.kyori.adventure.text.Component
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Metadata
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.EntityMeta
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket
import net.minestom.server.network.packet.server.play.EntityPositionPacket
import net.minestom.server.network.packet.server.play.SpawnEntityPacket
import java.util.UUID

class PacketHologramLine(
  private var position: Pos,
  private var hologramText: Component,
  private var player: Player
) : HologramLine {

  private val entityId: Int = Entity.generateId()
  private val entityUuid: UUID = UUID.randomUUID()
  private var lastPosition: Pos = position
  private var hologramsStatus = Status.NO_INTERACTION

  override fun doRender() {
    if(hologramsStatus == Status.NO_INTERACTION) {
      player.sendPacket(getSpawnEntityPacket())
      hologramsStatus = Status.SPAWNED
    }

    if(lastPosition != position) {
      player.sendPacket(getEntityPositionPacket())
    }

    player.sendPacket(getMetadataPacket(
      hologramText
    ))

    if(lastPosition != position) {
      lastPosition = position
    }
  }

  override fun remove() {
    player.sendPacket(getEntityDestroyPacket())
  }

  override fun updatePosition(newPosition: Pos) {
    lastPosition = position
    position = newPosition
  }

  override fun updateText(newText: Component) {
    hologramText = newText
  }

  private fun getSpawnEntityPacket(): SpawnEntityPacket {
    return SpawnEntityPacket(
      entityId, entityUuid, EntityType.ARMOR_STAND.id(),
      position, 0.0f, 0, 0, 0, 0
    )
  }

  private fun getMetadataPacket(component: Component): EntityMetaDataPacket {
    val metadata = Metadata(null)
    val entityMeta = EntityMeta(null, metadata)

    entityMeta.isCustomNameVisible = true
    entityMeta.customName = component
    entityMeta.isInvisible = true
    entityMeta.isHasNoGravity = true

    return EntityMetaDataPacket(
      entityId, metadata.entries
    )
  }

  private fun getEntityPositionPacket(): EntityPositionPacket {
    return EntityPositionPacket.getPacket(
      entityId, position, lastPosition, false
    )
  }

  private fun getEntityDestroyPacket(): DestroyEntitiesPacket {
    return DestroyEntitiesPacket(entityId)
  }

  enum class Status {
    NO_INTERACTION, SPAWNED
  }
}
