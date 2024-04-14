package com.daylifecraft.common.text

import net.minestom.server.entity.Player

class HardcodedText(private var string: String) : PlayerText {
  fun set(string: String) {
    this.string = string
  }

  override fun string(player: Player): String = string.replace(VAR_PLAYER_USERNAME, player.username)

  companion object {
    val EMPTY: HardcodedText = HardcodedText("")
    private const val VAR_PLAYER_USERNAME = "$(hardcodedText:playerUsername)"
  }
}
