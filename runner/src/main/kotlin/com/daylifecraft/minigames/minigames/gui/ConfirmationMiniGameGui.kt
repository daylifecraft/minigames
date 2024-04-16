package com.daylifecraft.minigames.minigames.gui

import com.daylifecraft.common.text.HardcodedText
import com.daylifecraft.minigames.gui.BindingGUI
import com.daylifecraft.minigames.gui.GuiItem
import com.daylifecraft.minigames.gui.GuiItem.PlayerInteractionHandler
import com.daylifecraft.minigames.minigames.settings.GeneralGameSettings
import com.daylifecraft.minigames.text.i18n.TranslateText
import com.daylifecraft.minigames.util.GuiUtil.isChest6SlotInEdge
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.function.Consumer

/**
 * Confirmation GUI for round startup. All players in round should confirm starting after the game
 * founded
 */
class ConfirmationMiniGameGui private constructor(
  player: Player,
  private val generalGameSettings: GeneralGameSettings,
  private val onPlayerAccept: PlayerInteractionHandler,
  private val onPlayerDeclined: PlayerInteractionHandler,
  private val onPlayerCloseInventory: Consumer<Player>,
) : BindingGUI(
  player,
  InventoryType.CHEST_6_ROW,
  TranslateText("rounds.new-round.invitation.title"),
) {
  private fun setup() {
    setItem(
      MINI_GAME_ITEM_SLOT,
      GuiItem(
        generalGameSettings.guiBlock,
        1,
        TranslateText(generalGameSettings.displayNameKey),
        TranslateText(
          "rounds.new-round.invitation.minigame.description",
          "minigameDisplayName" to TranslateText(generalGameSettings.displayNameKey),
        ),
        null,
      ),
    )

    setItem(
      ACCEPT_GAME_ITEM_SLOT,
      GuiItem(
        Material.GREEN_WOOL,
        1,
        TranslateText("rounds.new-round.invitation.accept-block.name"),
        HardcodedText.EMPTY,
        onPlayerAccept,
      ),
    )

    setItem(
      DECLINE_GAME_ITEM_SLOT,
      GuiItem(
        Material.RED_WOOL,
        1,
        TranslateText("rounds.new-round.invitation.decline-block.name"),
        HardcodedText.EMPTY,
        onPlayerDeclined,
      ),
    )
  }

  override fun renderEmptyItemStack(slot: Int, player: Player): ItemStack {
    if (isChest6SlotInEdge(slot)) {
      return BACKGROUND_R1
    }
    return super.renderEmptyItemStack(slot, player)
  }

  override fun viewerRemoved(player: Player) {
    MinecraftServer.getSchedulerManager()
      .scheduleNextTick { onPlayerCloseInventory.accept(player) }
  }

  companion object {
    private val BACKGROUND_R1 = ItemStack.of(Material.PURPLE_STAINED_GLASS_PANE).withDisplayName(Component.empty())

    private const val MINI_GAME_ITEM_SLOT = 22
    private const val ACCEPT_GAME_ITEM_SLOT = 38
    private const val DECLINE_GAME_ITEM_SLOT = 42

    /**
     * Open the GUI for player
     *
     * @param player Player instance
     * @param generalGameSettings MiniGame settings
     * @param onPlayerAccepted Function, that called on player click to 'accept'
     * @param onPlayerDeclined Function, that called on player click to `decline`
     * @param onPlayerClosedInventory Function, that called on player close inventory (no close
     * currently)
     */
    fun showConfirmationGui(
      player: Player,
      generalGameSettings: GeneralGameSettings,
      onPlayerAccepted: PlayerInteractionHandler,
      onPlayerDeclined: PlayerInteractionHandler,
      onPlayerClosedInventory: Consumer<Player>,
    ) {
      val confirmationMiniGameGui =
        ConfirmationMiniGameGui(
          player,
          generalGameSettings,
          onPlayerAccepted,
          onPlayerDeclined,
          onPlayerClosedInventory,
        )

      confirmationMiniGameGui.setup()
      confirmationMiniGameGui.show()
    }
  }
}
