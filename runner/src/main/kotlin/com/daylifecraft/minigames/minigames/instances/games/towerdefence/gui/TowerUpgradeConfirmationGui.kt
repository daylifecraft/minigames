package com.daylifecraft.minigames.minigames.instances.games.towerdefence.gui

import com.daylifecraft.minigames.gui.BindingGUI
import com.daylifecraft.minigames.gui.GuiItem
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerData
import com.daylifecraft.minigames.text.i18n.TranslateText
import com.daylifecraft.minigames.util.GuiUtil
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.function.Consumer

class TowerUpgradeConfirmationGui(
  player: Player,
  private val newTowerData: TowerData,
  private val onConfirm: Consumer<Player>,
) : BindingGUI(
  // TODO GUI Title
  player,
  InventoryType.CHEST_6_ROW,
  TranslateText("NONE"),
) {
  override fun renderEmptyItemStack(slot: Int, player: Player): ItemStack {
    if (GuiUtil.isChest6SlotInEdge(slot)) {
      return BACKGROUND_CYAN
    }

    return super.renderEmptyItemStack(slot, player)
  }

  fun setup() {
    setItem(
      22,
      GuiItem(
        material = newTowerData.guiItem!!,
        amount = 1,
        text = TranslateText(newTowerData.displayNameKey),
        lore = TranslateText(newTowerData.descriptionKey),
      ),
    )

    setItem(
      39,
      GuiItem(
        material = Material.GREEN_GLAZED_TERRACOTTA,
        amount = 1,
        text = TranslateText("menu.reusable.confirm.name"),
        lore = TranslateText(
          "games.towerdefence.upgrade.confirm.description",
          "upgradePrice" to newTowerData.cost.toString(),
        ),
      ) { _, player, _, _ ->
        onConfirm.accept(player)
        player.closeInventory()
        true
      },
    )

    setItem(
      41,
      GuiItem(
        material = Material.RED_GLAZED_TERRACOTTA,
        amount = 1,
        text = TranslateText("menu.reusable.deny.name"),
      ) { _, player, _, _ ->
        player.closeInventory()
        true
      },
    )
  }

  companion object {
    private val BACKGROUND_CYAN = ItemStack.of(Material.CYAN_STAINED_GLASS_PANE).withDisplayName(Component.empty())

    fun showConfirmationGui(player: Player, newTowerData: TowerData, onConfirm: Consumer<Player>) {
      val confirmationGui = TowerUpgradeConfirmationGui(player, newTowerData, onConfirm)

      confirmationGui.setup()
      confirmationGui.show()
    }
  }
}
