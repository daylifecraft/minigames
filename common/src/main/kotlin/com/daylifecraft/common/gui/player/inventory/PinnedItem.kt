package com.daylifecraft.common.gui.player.inventory

import com.daylifecraft.common.util.extensions.minestom.addListener
import net.minestom.server.entity.Player
import net.minestom.server.entity.Player.Hand
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.event.player.PlayerSwapItemEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.event.trait.PlayerEvent
import net.minestom.server.item.ItemStack

internal class PinnedItem(
  val owner: Player,
  val pinnedSlot: Int,
  val itemStack: ItemStack = owner.inventory.getItemStack(pinnedSlot),
  private val onLeftClick: () -> Unit = { },
  private val onRightClick: () -> Unit = { },
  private val onClickInInventory: (InventoryPreClickEvent) -> Unit = { },
) {

  private val events = EventNode
    .type("Slot $pinnedSlot", EventFilter.PLAYER)
    .addListener { event: PlayerSwapItemEvent ->
      if (event.player.heldSlot.toInt() == pinnedSlot) {
        event.isCancelled = true
      }
    }
    .addListener { event: PlayerHandAnimationEvent ->
      if (event.hand == Hand.MAIN && event.player.heldSlot.toInt() == pinnedSlot) {
        onLeftClick()
      }
    }
    .addListener { event: PlayerUseItemEvent ->
      if (event.hand == Hand.MAIN && event.player.heldSlot.toInt() == pinnedSlot) {
        event.isCancelled = true
        onRightClick()
      }
    }
    .addListener { event: InventoryPreClickEvent ->
      if (event.slot == pinnedSlot) {
        event.isCancelled = true
        onClickInInventory(event)
      }
    }

  fun attach(parent: EventNode<in PlayerEvent>) {
    parent.addChild(events)
  }

  fun detach() {
    events.parent?.removeChild(events)
  }

  fun refresh() {
    owner.inventory.setItemStack(pinnedSlot, itemStack)
  }
}
