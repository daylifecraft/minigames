package com.daylifecraft.minigames.instance

import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player

private const val DEFAULT_FLYING_SPEED = 0.05f

class InstancePlayerState {
  /**
   * The game mode of this state
   */
  var gameMode: GameMode = GameMode.SURVIVAL

  /**
   * The flying speed of this state
   */
  var flyingSpeed: Float = DEFAULT_FLYING_SPEED

  /**
   * Set the position of this state
   */
  var pos: Pos = Pos.ZERO

  /**
   * Apply this state to the specified player
   *
   * @param player player instance
   */
  fun applyToPlayer(player: Player) {
    player.setGameMode(gameMode)
    player.flyingSpeed = flyingSpeed
    player.respawnPoint = pos
    if (player.instance != null) {
      player.teleport(pos)
    }
    player.inventory.clear()
  }
}
