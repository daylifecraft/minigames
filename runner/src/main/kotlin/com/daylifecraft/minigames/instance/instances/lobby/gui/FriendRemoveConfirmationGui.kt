package com.daylifecraft.minigames.instance.instances.lobby.gui

import com.daylifecraft.minigames.database.DatabaseManager.getPlayerProfile
import com.daylifecraft.minigames.gui.BindingGUI
import com.daylifecraft.minigames.gui.GUI
import com.daylifecraft.minigames.gui.GuiItem
import com.daylifecraft.minigames.profile.player.PlayerProfile
import com.daylifecraft.minigames.text.i18n.TranslateText
import com.daylifecraft.minigames.util.GuiUtil.StaticUsernameSkullOwnerPreRenderer
import com.daylifecraft.minigames.util.GuiUtil.isChest6SlotInEdge
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

/** Gui for confirmation delete friend  */
class FriendRemoveConfirmationGui private constructor(
  player: Player,
  private val previousGui: GUI?,
  private val friend: PlayerProfile,
) : BindingGUI(
  player,
  InventoryType.CHEST_6_ROW,
  TranslateText(
    "menu.friends-list.confirm.player.name",
    VAR_TARGET_PLAYER to friend.username,
  ),
) {
  private val friendUsername = friend.username

  override fun renderEmptyItemStack(slot: Int, player: Player): ItemStack {
    if (isChest6SlotInEdge(slot)) {
      return BACKGROUND_RT1
    }
    return super.renderEmptyItemStack(slot, player)
  }

  private fun removeFriend() {
    getPlayerProfile(player)!!.removeFriend(friend.uuid)
  }

  private fun finishGui() {
    if (previousGui != null) {
      previousGui.show(player)
    } else {
      close()
    }
  }

  private fun setup() {
    setItem(
      slot = 22,
      GuiItem(
        material = Material.PLAYER_HEAD,
        amount = 1,
        text = translateWithTargetPlayer("menu.friends-list.confirm.player.name", friendUsername),
        lore = null,
        onInteract = GuiItem.NO_INTERACTION,
      ).setPreRenderer(StaticUsernameSkullOwnerPreRenderer(friendUsername)),
    )

    setItem(
      slot = 30,
      GuiItem(
        material = Material.RED_SHULKER_BOX,
        amount = 1,
        text = translateWithTargetPlayer("menu.friends-list.confirm.deny.name", friendUsername),
        lore = null,
      ) { _, _, _, _ ->
        finishGui()
        true
      },
    )

    setItem(
      slot = 32,
      GuiItem(
        material = Material.GREEN_SHULKER_BOX,
        amount = 1,
        text = translateWithTargetPlayer("menu.friends-list.confirm.accept.name", friendUsername),
        lore = null,
      ) { _, _, _, _ ->
        removeFriend()
        finishGui()
        true
      },
    )
  }

  companion object {
    private const val VAR_TARGET_PLAYER = "targetPlayer"
    private val BACKGROUND_RT1 = ItemStack.of(Material.RED_STAINED_GLASS_PANE).withDisplayName(Component.empty())

    fun showFriendRemoveConfirmationGui(
      player: Player,
      previousGui: GUI?,
      friendToRemove: PlayerProfile,
    ) {
      val gui = FriendRemoveConfirmationGui(player, previousGui, friendToRemove)
      gui.setup()
      gui.show()
    }

    private fun translateWithTargetPlayer(key: String, username: String): TranslateText = TranslateText(key, VAR_TARGET_PLAYER to username)
  }
}
