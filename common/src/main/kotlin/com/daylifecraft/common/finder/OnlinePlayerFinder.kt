package com.daylifecraft.common.finder

import net.minestom.server.entity.Player

class OnlinePlayerFinder : PlayerFinder {
  /**
   * Checks if player online
   *
   * @return true if player not online
   */
  val isPlayerIsNotOnline: Boolean

  /**
   * Checks if player is friend
   *
   * @return true if player is friend
   */
  val isPlayerIsFriend: Boolean

  constructor(player: Player, input: String) : super(player, input) {
    isPlayerIsNotOnline = false
    isPlayerIsFriend = false
  }

  constructor(
    input: String,
    samePlayer: Boolean,
    playerIsNotOnline: Boolean = false,
    playerIsFriend: Boolean = false,
  ) : super(input, samePlayer) {
    this.isPlayerIsNotOnline = playerIsNotOnline
    this.isPlayerIsFriend = playerIsFriend
  }
}
