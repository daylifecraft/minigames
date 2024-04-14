package com.daylifecraft.common.gui.player.inventory

import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.item.ItemStack

/**
 * Pinned item builder.
 *
 * Used to conveniently configure pinned item when setting it with [PlayerInventoryGui.setItem]
 */
class PinnedItemConfigurationBuilder {
  internal var onLeftClick: () -> Unit = { }
  internal var onRightClick: () -> Unit = { }
  internal var onClickInInventory: (InventoryPreClickEvent) -> Unit = { }

  /**
   * Sets left click handler
   *
   * This event is called when player left-clicking holding pinned item in the main hand.
   * Note that at this time this one will be called on pressing q button holding item.
   */
  fun onLeftClick(handler: () -> Unit) {
    onLeftClick = handler
  }

  /**
   * Sets right click handler
   *
   * This event is called when player right-clicking holding pinned item in the main hand.
   */
  fun onRightClick(handler: () -> Unit) {
    onRightClick = handler
  }

  /**
   * Sets inventory clicks handler
   *
   * This event is called when player any-click on pinned item in his inventory.
   */
  fun onClickInInventory(handler: (InventoryPreClickEvent) -> Unit) {
    onClickInInventory = handler
  }

  /**
   * Sets the same handler for leftClick and rightClick events
   */
  fun onAnyClick(handler: () -> Unit) {
    onLeftClick = handler
    onRightClick = handler
  }
}

/**
 * Gui that is being displayed in the inventory of the player.
 *
 * @param owner of this Gui
 */
class PlayerInventoryGui(
  private val owner: Player,
) {

  private val events = EventNode
    .type("PlayerInventoryGui of ${owner.uuid}", EventFilter.PLAYER) { _, eventPlayer ->
      eventPlayer.uuid == owner.uuid
    }

  private val items = mutableListOf<PinnedItem>()

  /**
   * Starts events handling related to this gui.
   * Such as clicks handling and canceling unwanted events like PlayerSwapItemEvent
   *
   * @param parent parent events node to which current will be attached.
   * Note that parent events node can apply its own filtering for events.
   * Such as ensuring that player is in correct instance
   */
  fun attachEvents(parent: EventNode<in Event>) {
    parent.addChild(events)
  }

  /**
   * Sets Item to specified slot in this GUI.
   *
   * @param slot in which item will be set.
   * @param item item to set.
   * @param configurationCallback callback for configuration of pinned item click handlers etc.
   */
  fun setItem(slot: Int, item: ItemStack, configurationCallback: PinnedItemConfigurationBuilder.() -> Unit) {
    val configurationBuilder = PinnedItemConfigurationBuilder()
    configurationCallback.invoke(configurationBuilder)

    val pinnedItem = PinnedItem(
      owner = owner,
      pinnedSlot = slot,
      itemStack = item,
      onLeftClick = configurationBuilder.onLeftClick,
      onRightClick = configurationBuilder.onRightClick,
      onClickInInventory = configurationBuilder.onClickInInventory,
    )

    pinnedItem.attach(events)
    pinnedItem.refresh()

    items.add(pinnedItem)
  }

  /**
   * Sets Item to specified slot in this GUI.
   *
   * @param slot in which item will be set.
   * @param item item to set.
   */
  fun setItem(slot: Int, item: ItemStack) {
    val pinnedItem = PinnedItem(
      owner = owner,
      pinnedSlot = slot,
      itemStack = item,
    )

    pinnedItem.attach(events)
    pinnedItem.refresh()

    items.add(pinnedItem)
  }

  /**
   * Detaches all specific items event handlers from current [events] node.
   */
  fun detachEvents() {
    items.forEach(PinnedItem::detach)
    events.parent?.removeChild(events)
    items.clear()
  }
}
