package com.daylifecraft.common.finder

import net.minestom.server.entity.Player

// TODO it would be nice to split this by constructors (maybe use sealed classes)
open class PlayerFinder {
  val player: Player?
  val input: String
  val isSamePlayer: Boolean
  val isPlayerNull: Boolean
    get() = player == null

  constructor(player: Player, input: String) {
    this.player = player
    this.input = input
    isSamePlayer = false
  }

  constructor(input: String, samePlayer: Boolean) {
    player = null

    this.input = input
    this.isSamePlayer = samePlayer
  }
}
