package com.daylifecraft.minigames.gui

import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.item.ItemStack
import org.jetbrains.annotations.ApiStatus

class ViewerInventory(
  private val parent: GUI,
  private val viewer: Player,
  val inventory: Inventory,
) {
  /** initialize default inventory conditional (do not call double)  */
  fun initializeDefaultInventoryConditional() {
    inventory.addInventoryCondition(parent::acceptAction)
  }

  /**
   * Places an itemstack in an inventory slot
   *
   * @param slot slot index
   * @param itemStack itemstack to set
   */
  private fun setItemStack(slot: Int, itemStack: ItemStack) {
    inventory.setItemStack(slot, itemStack)
  }

  /** Checks if the inventory is open by the viewer  */
  private val isOpenedByViewer: Boolean
    get() = viewer.openInventory === inventory

  /** Clear inventory and remove viewer from it  */
  fun dispose() {
    inventory.removeViewer(viewer)
    inventory.clear()
  }

  /** Open inventory for viewer  */
  fun open() {
    viewer.openInventory(inventory)
  }

  /** Close inventory if viewer has it open  */
  fun closeIfOpenThis() {
    if (isOpenedByViewer) {
      viewer.closeInventory()
    }
  }

  /** Calls this method every tick  */
  @ApiStatus.OverrideOnly
  fun tick() {
    // maybe override
  }

  /**
   * Redraw slot by index
   *
   * @param slot slot index
   */
  fun redraw(slot: Int) {
    setItemStack(slot, parent.renderItemStack(slot, viewer))
  }
}
