package com.daylifecraft.minigames.gui.player.inventory

import com.daylifecraft.common.gui.player.inventory.PlayerInventoryGui
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import net.minestom.server.entity.Player
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.event.player.PlayerSwapItemEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID
import kotlin.test.assertEquals

class PlayerInventoryGuiTest {
  @Test
  fun testEventsLifecycle() {
    val playerInventoryGui = PlayerInventoryGui(player)

    val parentNode = spyk(EventNode.all("parent"))

    playerInventoryGui.attachEvents(parentNode)
    verify(exactly = 1) { parentNode.addChild(any()) }

    playerInventoryGui.detachEvents()
    verify(exactly = 1) { parentNode.removeChild(any()) }
  }

  @Test
  fun testEventsCancellation() {
    val playerInventoryGui = PlayerInventoryGui(player).apply {
      setItem(0, itemStack)
    }

    val parentNode = spyk(EventNode.all("parent"))

    playerInventoryGui.attachEvents(parentNode)

    val itemUseEvent = PlayerUseItemEvent(player, Player.Hand.MAIN, itemStack)
    parentNode.call(itemUseEvent)
    assert(itemUseEvent.isCancelled)

    val itemSwapEvent = PlayerSwapItemEvent(player, cursorItem, itemStack)
    parentNode.call(itemSwapEvent)
    assert(itemSwapEvent.isCancelled)
  }

  @ParameterizedTest
  @MethodSource("leftClicks")
  fun testLeftClick(event: PlayerHandAnimationEvent, expectedCallsCount: Int) {
    var leftClickCalled = 0

    val playerInventoryGui = PlayerInventoryGui(player).apply {
      setItem(0, itemStack) {
        onLeftClick {
          leftClickCalled++
        }
      }
    }

    val parentNode = EventNode.all("parent")

    playerInventoryGui.attachEvents(parentNode)

    parentNode.call(event)
    assertEquals(expectedCallsCount, leftClickCalled, "Inventory must be clicked exactly $expectedCallsCount times")
  }

  @ParameterizedTest
  @MethodSource("rightClicks")
  fun testRightClick(event: PlayerUseItemEvent, expectedCallsCount: Int) {
    var rightClickCalled = 0

    val playerInventoryGui = PlayerInventoryGui(player).apply {
      setItem(0, itemStack) {
        onRightClick {
          rightClickCalled++
        }
      }
    }

    val parentNode = spyk(EventNode.all("parent"))

    playerInventoryGui.attachEvents(parentNode)

    parentNode.call(event)
    assertEquals(expectedCallsCount, rightClickCalled, "Inventory must be clicked exactly $expectedCallsCount times")
  }

  @ParameterizedTest
  @MethodSource("inventoryClicks")
  fun testInventoryClick(event: InventoryPreClickEvent, expectedCallsCount: Int) {
    var inventoryClickCalled = 0

    val playerInventoryGui = PlayerInventoryGui(player).apply {
      setItem(0, itemStack) {
        onClickInInventory {
          inventoryClickCalled++
        }
      }
    }

    val parentNode = spyk(EventNode.all("parent"))

    playerInventoryGui.attachEvents(parentNode)

    parentNode.call(event)
    assertEquals(expectedCallsCount, inventoryClickCalled, "Inventory must be clicked exactly $expectedCallsCount times")
  }

  companion object {
    private val player = mockk<Player>(relaxed = true)
    private val anotherPlayer = mockk<Player>(relaxed = true)

    val itemStack = ItemStack.of(Material.DIAMOND)
    val cursorItem = ItemStack.of(Material.AIR)

    @JvmStatic
    @BeforeAll
    fun setup() {
      every { player.inventory } returns mockk(relaxed = true)
      every { player.uuid } returns UUID.randomUUID()

      every { anotherPlayer.inventory } returns mockk(relaxed = true)
      every { anotherPlayer.uuid } returns UUID.randomUUID()
    }

    @JvmStatic
    fun leftClicks() = listOf(
      Arguments.of(
        PlayerHandAnimationEvent(player, Player.Hand.MAIN),
        1,
      ),
      Arguments.of(
        PlayerHandAnimationEvent(anotherPlayer, Player.Hand.MAIN),
        0,
      ),
      Arguments.of(
        PlayerHandAnimationEvent(player, Player.Hand.OFF),
        0,
      ),
      Arguments.of(
        PlayerHandAnimationEvent(anotherPlayer, Player.Hand.OFF),
        0,
      ),
    )

    @JvmStatic
    fun rightClicks() = listOf(
      Arguments.of(
        PlayerUseItemEvent(player, Player.Hand.MAIN, itemStack),
        1,
      ),
      Arguments.of(
        PlayerUseItemEvent(player, Player.Hand.OFF, itemStack),
        0,
      ),
      Arguments.of(
        PlayerUseItemEvent(anotherPlayer, Player.Hand.MAIN, itemStack),
        0,
      ),
      Arguments.of(
        PlayerUseItemEvent(anotherPlayer, Player.Hand.OFF, itemStack),
        0,
      ),
    )

    @JvmStatic
    fun inventoryClicks() = listOf(
      Arguments.of(
        InventoryPreClickEvent(null, player, 0, ClickType.LEFT_CLICK, itemStack, cursorItem),
        1,
      ),
      Arguments.of(
        InventoryPreClickEvent(null, anotherPlayer, 0, ClickType.LEFT_CLICK, itemStack, cursorItem),
        0,
      ),
      Arguments.of(
        InventoryPreClickEvent(null, player, 1, ClickType.LEFT_CLICK, itemStack, cursorItem),
        0,
      ),
      Arguments.of(
        InventoryPreClickEvent(null, anotherPlayer, 1, ClickType.LEFT_CLICK, itemStack, cursorItem),
        0,
      ),
      Arguments.of(
        InventoryPreClickEvent(null, player, 0, ClickType.RIGHT_CLICK, itemStack, cursorItem),
        1,
      ),
      Arguments.of(
        InventoryPreClickEvent(null, anotherPlayer, 0, ClickType.SHIFT_CLICK, itemStack, cursorItem),
        0,
      ),
      Arguments.of(
        InventoryPreClickEvent(null, player, 1, ClickType.LEFT_DRAGGING, itemStack, cursorItem),
        0,
      ),
    )
  }
}
