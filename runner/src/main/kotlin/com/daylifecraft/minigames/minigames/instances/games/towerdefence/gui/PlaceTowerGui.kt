package com.daylifecraft.minigames.minigames.instances.games.towerdefence.gui

import com.daylifecraft.minigames.gui.GuiItem
import com.daylifecraft.minigames.instance.instances.lobby.gui.AbstractListGui
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerData
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenseManager
import com.daylifecraft.minigames.text.i18n.TranslateText
import com.daylifecraft.minigames.util.GuiUtil.isChest6SlotInEdge
import com.daylifecraft.minigames.util.GuiUtil.isChest6SlotInLowerRow
import com.daylifecraft.minigames.util.GuiUtil.isChest6SlotInUpperRow
import com.daylifecraft.minigames.util.GuiUtil.isNotLR
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class PlaceTowerGui private constructor(player: Player, private val towerDefenceInstance: TowerDefenceInstance) : AbstractListGui(player, TranslateText("menu.games.towerdefence.builder.title")) {

  override val listSize: Int
    get() = list.size

  override val list: Array<GuiItem>
    get() = TowerDefenseManager.get()
      .getLoadedTowersData()
      .filter { it.level == 0 }
      .map { TowerItem(it) }
      .toTypedArray()

  fun setup() {
    setupAbstractListGui()

    setItem(
      slot = 37,
      GuiItem(
        material = Material.ENDER_PEARL,
        amount = 1,
        text = TranslateText("menu.reusable.close.name"),
        null,
      ) { _, player, _, _ ->
        player.closeInventory()
        true
      },
    )

    setItem(
      slot = 39,
      guiItem = UsageToPageButton(
        material = Material.LANTERN,
        "menu.reusable.left-page-button.name",
        "previousPage",
        -1,
      ),
    )

    setItem(
      slot = 40,
      guiItem = CurrentPageIndicator(
        "menu.reusable.current-page.name",
        "currentPage",
        material = Material.PAPER,
      ),
    )

    setItem(
      slot = 41,
      guiItem = UsageToPageButton(
        material = Material.SOUL_LANTERN,
        "menu.reusable.right-page-button.name",
        "nextPage",
        1,
      ),
    )
  }

  override fun renderEmptyItemStack(slot: Int, player: Player): ItemStack {
    if (isChest6SlotInEdge(slot)) {
      if (isChest6SlotInUpperRow(slot)) {
        return if (slot % 2 == 0) {
          BACKGROUND_YELLOW
        } else {
          BACKGROUND_LIME
        }
      }

      if (isChest6SlotInLowerRow(slot)) {
        return if (slot % 2 == 0) {
          BACKGROUND_LIME
        } else {
          BACKGROUND_YELLOW
        }
      }

      return if (slot in intArrayOf(9, 17, 36, 44)) {
        BACKGROUND_LIME
      } else {
        BACKGROUND_YELLOW
      }
    }
    return super.renderEmptyItemStack(slot, player)
  }

  inner class TowerItem internal constructor(private val towerData: TowerData) : GuiItem() {
    init {
      material = towerData.guiItem ?: Material.BARRIER
      text = TranslateText(towerData.displayNameKey)
      lore = TranslateText(towerData.descriptionKey)
    }

    override fun playerClicked(item: GuiItem, player: Player, slot: Int, clickType: ClickType): Boolean {
      if (clickType.isNotLR) {
        return true
      }

      towerDefenceInstance.onPlayerTryPlaceTower(player, towerData)
      player.closeInventory()
      return true
    }
  }

  companion object {
    private val BACKGROUND_YELLOW = ItemStack.of(Material.YELLOW_STAINED_GLASS_PANE).withDisplayName(Component.empty())
    private val BACKGROUND_LIME = ItemStack.of(Material.LIME_STAINED_GLASS_PANE).withDisplayName(Component.empty())

    fun showPlaceTowerGui(player: Player, towerDefenceInstance: TowerDefenceInstance) {
      val placeTowerGui = PlaceTowerGui(player, towerDefenceInstance)
      placeTowerGui.setup()
      placeTowerGui.show()
    }
  }
}
