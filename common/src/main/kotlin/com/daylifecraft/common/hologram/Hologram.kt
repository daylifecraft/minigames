package com.daylifecraft.common.hologram

import com.daylifecraft.common.text.PlayerText
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player

interface Hologram {

  fun setHologramTextWithLinesRecreation(hologramText: PlayerText)

  fun updateText(hologramText: PlayerText)

  fun addViewer(player: Player)

  fun removeViewer(player: Player)

  fun doRender()

  fun updatePositionWithoutRender(newPosition: Pos)

  fun remove()
}
