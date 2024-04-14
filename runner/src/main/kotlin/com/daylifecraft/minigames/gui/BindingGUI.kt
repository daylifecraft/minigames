package com.daylifecraft.minigames.gui

import com.daylifecraft.common.text.PlayerText
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType

/**
 * Binding gui this is gui created specially for one player and provided simply methods for manage
 * gui with one player
 */
abstract class BindingGUI protected constructor(
  protected val player: Player,
  inventoryType: InventoryType,
  title: PlayerText,
) : GUI(inventoryType, title) {
  /** Show GUI for binding player  */
  fun show() {
    show(player)
  }

  /** Close GUI for binding player  */
  protected fun close() {
    close(player)
  }

  /**
   * Check if player is gui binder
   *
   * @param player player instance
   * @return return true if player is binder
   */
  fun isBinder(player: Player): Boolean = player === this.player
}
