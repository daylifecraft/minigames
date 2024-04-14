package com.daylifecraft.common.finder

import net.minestom.server.entity.Player

class FriendPlayerFinder : PlayerFinder {
  /**
   * Checks if player not friend
   *
   * @return true if not friend
   */
  val isNotFriend: Boolean

  /**
   * Checks if player has disabled PM
   *
   * @return true player has disabled PM
   */
  val isDisabledPm: Boolean

  /**
   * Checks if player is not online
   *
   * @return true if player is not online
   */
  val isNotOnline: Boolean

  /**
   * Checks if player muted
   *
   * @return true if player is muted
   */
  val isMuted: Boolean

  constructor(player: Player, input: String) : super(player, input) {
    isNotFriend = false
    isDisabledPm = false
    isNotOnline = false
    isMuted = false
  }

  constructor(
    input: String,
    samePlayer: Boolean,
    notFriend: Boolean = false,
    disabledPm: Boolean = false,
    muted: Boolean = false,
    notOnline: Boolean = false,
  ) : super(input, samePlayer) {
    isNotFriend = notFriend
    isDisabledPm = disabledPm
    isMuted = muted
    isNotOnline = notOnline
  }
}
