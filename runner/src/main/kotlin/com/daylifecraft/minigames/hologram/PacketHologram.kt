package com.daylifecraft.minigames.hologram

import com.daylifecraft.common.text.PlayerText
import net.kyori.adventure.text.Component
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Metadata
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.EntityMeta
import net.minestom.server.entity.metadata.other.ArmorStandMeta
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket
import net.minestom.server.network.packet.server.play.EntityPositionPacket
import net.minestom.server.network.packet.server.play.SpawnEntityPacket
import java.util.UUID

internal class PacketHologram(
  private var position: Pos,
  private var hologramText: PlayerText,
  viewers: List<Player>
) : Hologram {

  private val entityId: Int = Entity.generateId()
  private val entityUuid: UUID = UUID.randomUUID()
  private var lastPosition: Pos = position

  private val hologramsStatus: MutableMap<Player, Status> = viewers
    .associateWith { Status.NO_INTERACTION }.toMutableMap()

  override fun setHologramText(hologramText: PlayerText) {
    this.hologramText = hologramText
  }

  override fun addViewer(player: Player) {
    hologramsStatus[player] = Status.NO_INTERACTION
  }

  override fun removeViewer(player: Player) {
    hologramsStatus.remove(player)

    player.sendPacket(getEntityDestroyPacket())
  }

  override fun updatePositionWithoutRender(newPosition: Pos) {
    lastPosition = position
    position = newPosition
  }

  override fun doRender() {
    hologramsStatus.toMap().forEach { (player, status) ->
      if(status == Status.NO_INTERACTION) {
        player.sendPacket(getSpawnEntityPacket())
        hologramsStatus[player] = Status.SPAWNED
      }

      if(lastPosition != position) {
        player.sendPacket(getEntityPositionPacket())
      }

      player.sendPacket(getMetadataPacket(
        hologramText.miniMessage(player)
      ))
    }

    if(lastPosition != position) {
      lastPosition = position
    }
  }

  override fun remove() {
    hologramsStatus.forEach {
      (player, _) -> player.sendPacket(getEntityDestroyPacket())
    }
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
//    entityMeta.customName = component
    entityMeta.isInvisible = true
    entityMeta.isHasNoGravity = true

    metadata.setIndex(2, Metadata.OptChat(component))

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
