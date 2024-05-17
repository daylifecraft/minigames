package com.daylifecraft.common.hologram

import net.kyori.adventure.text.Component
import net.minestom.server.coordinate.Pos


interface HologramLine {

  fun doRender()

  fun remove()

  fun updatePosition(newPosition: Pos)

  fun updateText(newText: Component)

}
