package com.daylifecraft.common.text

import com.daylifecraft.common.util.extensions.miniMessage
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player

interface PlayerText {
  fun string(player: Player): String

  fun miniMessage(player: Player): Component = string(player).miniMessage()
}
