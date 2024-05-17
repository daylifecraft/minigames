package com.daylifecraft.minigames.minigames.instances.games.towerdefence.gui

import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.minigames.gui.BindingGUI
import com.daylifecraft.minigames.gui.GuiItem
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerData
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import com.daylifecraft.minigames.text.i18n.TranslateText
import com.daylifecraft.minigames.util.GuiUtil.isChest6SlotInEdge
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class TowerManagementGui private constructor(
  player: Player,
  val towerDefenceInstance: TowerDefenceInstance,
  private val towerData: TowerData,
) : BindingGUI(player, InventoryType.CHEST_6_ROW, TranslateText("menu.games.towerdefence.manage_tower.title")) {

  private val upgradeItemsSlots: Map<Int, Array<Int>> = mapOf(
    1 to arrayOf(22),
    2 to arrayOf(21, 23),
    3 to arrayOf(20, 22, 24),
  )

  fun setup() {
    if (towerData.upgradePath != null) {
      val upgradesSize = towerData.upgradePath.upgradeItems.size
      val slots = upgradeItemsSlots[upgradesSize] ?: emptyArray()

      for (index in 0..<upgradesSize) {
        val currentTowerData = towerData.upgradePath.upgradeItems[index].getTowerData()

        if (currentTowerData == null) {
          createLogger().debug("Error while rendering Tower Management GUI and getting upgrade path for: " + towerData.towerId)
          continue
        }

        setItem(
          slots[index],
          GuiItem(
            material = currentTowerData.guiItem!!,
            amount = 1,
            text = TranslateText(currentTowerData.displayNameKey),
            lore = TranslateText(currentTowerData.descriptionKey),
          ) { _, player, _, _ ->
            onPlayerTryUpgradeTower(player, currentTowerData)
            true
          },
        )
      }
    }

    setItem(
      37,
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
      40,
      GuiItem(
        material = towerData.guiItem!!,
        amount = 1,
        text = TranslateText(towerData.displayNameKey),
        lore = TranslateText(towerData.descriptionKey),
      ),
    )

    if (towerData.ownerData?.first == player.uuid) {
      setItem(
        43,
        GuiItem(
          material = Material.REDSTONE,
          amount = 1,
          text = TranslateText("menu.games.towerdefence.manage_tower.destroy.name"),
          lore = TranslateText(
            "menu.games.towerdefence.manage_tower.destroy.description",
            "sellPrice" to towerData.getSellPrice().toString(),
          ),
        ) { _, player, _, _ ->
          onPlayerTrySellTower(player, towerData)
          true
        },
      )
    }
  }

  private fun onPlayerTrySellTower(player: Player, towerData: TowerData) {
    TowerRemoveConfirmationGui.showConfirmationGui(player, towerData) {
      towerDefenceInstance.towerDefenceEconomy.addBalance(player, towerData.getSellPrice())
      towerDefenceInstance.removeTower(towerData, clearBlocks = true)
    }
  }

  private fun onPlayerTryUpgradeTower(player: Player, newTowerData: TowerData) {
    if (!towerDefenceInstance.towerDefenceEconomy.hasOnBalance(player, newTowerData.cost)) {
      player.sendMessage("You don`t have enough gold on balance")
      return
    }

    TowerUpgradeConfirmationGui.showConfirmationGui(player, newTowerData) {
      if (!towerDefenceInstance.towerDefenceEconomy.hasOnBalance(player, newTowerData.cost)) {
        player.sendMessage("You don`t have enough gold on balance")
        return@showConfirmationGui
      }

      towerDefenceInstance.towerDefenceEconomy.addBalance(player, -newTowerData.cost)

      towerDefenceInstance.replaceTowerByNew(towerData, newTowerData)
    }
  }

  override fun renderEmptyItemStack(slot: Int, player: Player): ItemStack {
    if (isChest6SlotInEdge(slot)) {
      return if (slot in intArrayOf(1, 7, 9, 17, 36, 44, 46, 52)) {
        BACKGROUND_RED
      } else {
        BACKGROUND_ORANGE
      }
    }
    return super.renderEmptyItemStack(slot, player)
  }

  companion object {
    private val BACKGROUND_ORANGE = ItemStack.of(Material.ORANGE_STAINED_GLASS_PANE).withDisplayName(Component.empty())
    private val BACKGROUND_RED = ItemStack.of(Material.RED_STAINED_GLASS_PANE).withDisplayName(Component.empty())

    fun showTowerManagementGui(player: Player, towerDefenceInstance: TowerDefenceInstance, towerData: TowerData) {
      val towerManagementGui = TowerManagementGui(player, towerDefenceInstance, towerData)
      towerManagementGui.setup()
      towerManagementGui.show()
    }
  }
}
