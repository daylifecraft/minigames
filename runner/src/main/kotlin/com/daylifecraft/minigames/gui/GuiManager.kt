package com.daylifecraft.minigames.gui

import com.daylifecraft.minigames.Init
import net.minestom.server.entity.Player
import net.minestom.server.utils.validate.Check
import java.util.UUID

class GuiManager {
  private val openedGuis = HashMap<UUID, GUI>()

  private fun getPlayerGui(player: Player): GUI? = openedGuis[player.uuid]

  /**
   * Close a player's open inventory
   *
   * @param player player instance
   * @param noCloseCurrently true, if you need to close inventory correctly
   */
  fun close(player: Player, noCloseCurrently: Boolean) {
    val gui = getPlayerGui(player)
    gui?.removeViewer(player, noCloseCurrently)

    openedGuis.remove(player.uuid)
  }

  /**
   * Remove inventory from the server guis, but do not close it
   *
   * @param player player instance
   */
  fun closeWithoutNotify(player: Player) {
    Check.notNull(player, "player can't be null.")
    openedGuis.remove(player.uuid)
  }

  /**
   * Add player to gui but don't open it for player
   *
   * @param player player instance
   * @param gui gui instance
   */
  private fun showWithoutNotify(player: Player, gui: GUI) {
    close(player, true)
    openedGuis[player.uuid] = gui
  }

  /**
   * Show gui to player
   *
   * @param player player instance
   * @param gui gui instance
   */
  fun show(player: Player, gui: GUI) {
    showWithoutNotify(player, gui)
    gui.addViewer(player)
  }

  /**
   * Process tick for player gui
   *
   * @param player player instance
   */
  fun tick(player: Player) {
    val playerGui = getPlayerGui(player)
    playerGui?.tick(player)
  }

  companion object {
    /**
     * Gets gui manager
     *
     * @return gui manager
     */
    fun get(): GuiManager = Init.guiManager
  }
}
