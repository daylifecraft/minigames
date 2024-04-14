package com.daylifecraft.minigames.instance

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.trait.EntityEvent
import net.minestom.server.event.trait.InstanceEvent
import net.minestom.server.instance.Instance
import net.minestom.server.network.packet.server.play.TeamsPacket
import net.minestom.server.scoreboard.Team

object InstanceUtil {
  const val DAY_TIME = 12000L

  /**
   * Move all specified players to give instance
   *
   * @param players array with player instances
   * @param instance instance, where need to move
   */
  @JvmStatic
  fun moveAllPlayersToInstance(players: Array<Player>, instance: AbstractCraftInstance) {
    for (player in players) {
      player.setInstance(instance.instance)
    }
  }

  /**
   * @see .isEventRelated
   */
  @JvmStatic
  fun isEventRelated(event: Event, instance: AbstractCraftInstance): Boolean = isEventRelated(event, instance.instance)

  /**
   * Is the event related to the instance
   *
   *
   * 2023: used by a global listener to pass an event to an instance (CraftInstance) if it is
   * associated with it (returned true)
   */
  @JvmStatic
  fun isEventRelated(event: Event, instance: Instance): Boolean {
    if (event is InstanceEvent &&
      (event.instance === instance)
    ) {
      return true
    }

    return (
      event is EntityEvent &&
        (event.entity.instance === instance)
      )
  }

  /**
   * Check if an entity exists in an instance
   *
   * @param entity entity instance
   * @param instance instance
   */
  @JvmStatic
  fun isEntityInInstance(entity: Entity, instance: Instance): Boolean = entity.instance === instance

  /**
   * Check if an entity exists in a craft instance
   *
   * @param entity entity instance
   * @param instance craft instance
   */
  fun isEntityInInstance(entity: Entity, instance: AbstractCraftInstance): Boolean = isEntityInInstance(entity, instance.instance)

  /**
   * Create team with a specific collision rule
   *
   * @param name team name
   * @param collisionRule collision rule
   */
  @JvmStatic
  fun createCollisionTeam(name: String?, collisionRule: TeamsPacket.CollisionRule?): Team = MinecraftServer.getTeamManager()
    .createBuilder(name!!)
    .collisionRule(collisionRule)
    .build()
}
