package com.daylifecraft.common.hologram

import com.daylifecraft.common.text.PlayerText
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player

class HologramManager {

  companion object {

    fun createHologram(position: Pos, hologramText: PlayerText, viewers: List<Player>): Hologram {
      return PacketHologram(position, hologramText, viewers)
    }

  }

}
