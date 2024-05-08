package com.daylifecraft.minigames.hologram

import com.daylifecraft.common.text.PlayerText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentIteratorType
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player

internal class PacketHologram(
  private var position: Pos,
  private var hologramText: PlayerText,
  viewers: List<Player>
) : Hologram {

  private val hologramLines: MutableMap<Player, MutableList<HologramLine>> = viewers.associateWith { player ->
    createLinesForPlayer(player)
  }.toMutableMap()

  override fun setHologramTextWithLinesRecreation(hologramText: PlayerText) {
    this.hologramText = hologramText
    hologramLines.forEach { (player, lines) ->
      lines.map { it.remove() }
      lines.clear()

      lines.addAll(createLinesForPlayer(player))
    }
  }

  override fun updateText(hologramText: PlayerText) {
    this.hologramText = hologramText
    hologramLines.forEach { (player, lines) ->
      val textLines = hologramText.string(player).split("\n").reversed()
      for(index in 0..<lines.count()) {
        lines[index].updateText(
          MiniMessage.miniMessage().deserialize(textLines[index])
        )
      }
    }
  }

  override fun addViewer(player: Player) {
    if(player in hologramLines) {
      throw IllegalArgumentException("This player already in viewers")
    }

    hologramLines[player] = createLinesForPlayer(player)
  }

  override fun removeViewer(player: Player) {
    if(player !in hologramLines) {
      throw IllegalArgumentException("This player not in viewers")
    }
    hologramLines[player]?.forEach { line -> line.remove() }
    hologramLines.remove(player)
  }

  override fun updatePositionWithoutRender(newPosition: Pos) {
    hologramLines.values.forEach { line ->
      for(index in 0..<line.count()) {
        line[index].updatePosition(
          newPosition.add(0.0, LINE_POSITION_MODIFIER * (index + 1), 0.0)
        )
      }
    }
  }

  override fun doRender() {
    hologramLines.values.flatten().forEach {
      it.doRender()
    }
  }

  override fun remove() {
    hologramLines.values.flatten().forEach { line -> line.remove() }
  }

  private fun createLinesForPlayer(player: Player): MutableList<HologramLine> {
    val lines = hologramText.string(player).split("\n").reversed()
    val result = mutableListOf<HologramLine>()
    for(lineNumber in 0..<lines.count()) {
      result.add(PacketHologramLine(
        position.add(0.0, LINE_POSITION_MODIFIER * (lineNumber + 1), 0.0),
        MiniMessage.miniMessage().deserialize(lines[lineNumber]),
        player))
    }

    return result
  }

  companion object {
    private const val LINE_POSITION_MODIFIER = 0.2
  }
}
