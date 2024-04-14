package com.daylifecraft.minigames.gui

import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.text.PlayerText
import com.daylifecraft.minigames.util.sendStackTrace
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.inventory.condition.InventoryConditionResult
import net.minestom.server.item.ItemStack
import net.minestom.server.utils.validate.Check
import org.jetbrains.annotations.ApiStatus
import java.util.UUID

/** GUI instance  */
abstract class GUI protected constructor(
  private val inventoryType: InventoryType,
  private val title: PlayerText,
) {
  private val items = arrayOfNulls<GuiItem>(inventoryType.size)
  private val viewers = HashMap<UUID, ViewerInventory>()

  // calls every tick by PlayerTickEvent if Player gui opened
  open fun tick(player: Player) {
    val viewerInventory = getViewerInventory(player)
    viewerInventory?.tick()
  }

  /**
   * Calls by any viewer inventory conditional
   *
   *
   * TODO 09.08.2023 item sticks to cursor when middle clicked in creative mode
   */
  fun acceptAction(
    player: Player,
    slot: Int,
    clickType: ClickType,
    result: InventoryConditionResult,
  ) {
    sendStackTrace(player, "slot=$slot; type=$clickType")

    // is slot is negative (-999 = drop)
    if (slot < 0) {
      result.isCancel = true
      return
    }

    val slotItem = getItem(slot)
    if (slotItem == null) {
      if (isCancelNullItemExecution(player, slot, clickType, result)) {
        // cancel item executions with background items (see #renderEmptyItemStack(...))
        result.isCancel = true
      }
      return
    }

    val c = slotItem.playerClicked(slotItem, player, slot, clickType)
    if (c) {
      result.isCancel = true
    }
  }

  /**
   * Show this gui to player
   *
   * @param player player instance
   */
  fun show(player: Player) {
    guiManager.show(player, this)
  }

  /**
   * Close this gui for player
   *
   * @param player player instance
   */
  fun close(player: Player) {
    guiManager.close(player, false)
  }

  fun addViewer(player: Player) {
    sendStackTrace(player)

    val inv = createInventory(player)
    viewers[player.uuid] = inv
    inv.open()
    viewerAdded(player)
  }

  /**
   * Remove player from gui as viewer
   *
   * @param player player instance
   */
  protected fun removeViewer(player: Player) {
    removeViewer(player, false)
  }

  fun removeViewer(player: Player, noCloseCurrently: Boolean) {
    if (!isViewer(player)) {
      LOGGER.debug("WARN GUI::removeViewer received player not is viewer!")
      return
    }
    sendStackTrace(player, "noCloseCurrently=$noCloseCurrently")
    val inv = getViewerInventory(player)
    if (!noCloseCurrently) {
      inv!!.closeIfOpenThis()
    }
    viewers.remove(player.uuid)
    inv!!.dispose()
    viewerRemoved(player)
  }

  /**
   * Get inventory for viewer
   *
   * @param viewer player instance
   */
  private fun getViewerInventory(viewer: Player): ViewerInventory? = viewers[viewer.uuid]

  /** REDRAW ALL SLOTS FOR ANY VIEWER (so performance operation...)  */
  fun redraw() {
    for (player in getViewers()) {
      val inv = getViewerInventory(player)
      updateInventory(player, inv!!.inventory)
    }
  }

  /**
   * redraw slot for all viewers
   *
   * @param slot slot index
   */
  fun redrawSlotForAll(slot: Int) {
    for (player in getViewers()) {
      redrawSlot(slot, player)
    }
  }

  /**
   * redraw slot for one viewer
   *
   * @param slot slot index
   * @param player player instance
   */
  protected fun redrawSlot(slot: Int, player: Player) {
    sendStackTrace(player, "redraw for player. slot=$slot")

    val inv = getViewerInventory(player)
    if (inv != null) {
      inv.redraw(slot)
    } else {
      LOGGER.debug("WARNING: Gui::redraw(slot, player) received player not contains in viewers")
    }
  }

  /**
   * Gets all gui viewers
   *
   * @return set of viewers
   */
  private fun getViewers(): Set<Player> {
    val set = HashSet<Player>()
    for (uuid in viewers.keys) {
      val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid)
      player?.let { set.add(it) }
    }
    return set
  }

  /**
   *
   *
   * <h2>Set to all inventory slot result of [.renderItemStack]</h2>
   *
   * <br></br>
   *
   * <h3>Also maybe interpreted as raw redrawAllSlots</h3>
   */
  private fun updateInventory(player: Player, inv: Inventory) {
    var slot = 0
    while (slot < items.size) {
      val itemStack = renderItemStack(slot, player)
      inv.setItemStack(slot, itemStack)
      slot++
    }
  }

  /**
   *
   *
   * <h2>Create Minestorm-inventory for player by this GUI inventoryType and (title with player)
   </h2> *
   *
   * <h3>Add to this inventory condition 'this::acceptAction'</h3>
   *
   * <h3>call [.updateInventory] for created Minestorm-inventory</h3>
   */
  private fun createInventory(player: Player): ViewerInventory {
    val inventory = Inventory(inventoryType, title.miniMessage(player))
    updateInventory(player, inventory)

    return createViewerInventoryWrapper(this, player, inventory)
  }

  fun renderItemStack(slot: Int, player: Player): ItemStack {
    val item = getItem(slot) ?: return renderEmptyItemStack(slot, player)
    return item.renderForPlayer(player)
  }

  @ApiStatus.OverrideOnly
  protected fun createViewerInventoryWrapper(
    parent: GUI?,
    player: Player?,
    inventory: Inventory?,
  ): ViewerInventory {
    val viewerInventory = ViewerInventory(parent!!, player!!, inventory!!)

    viewerInventory.initializeDefaultInventoryConditional()
    return viewerInventory
  }

  /**
   * Override this if we need change AIR in guis to other item.
   *
   * @param slot slot index
   * @param player player instance
   */
  @ApiStatus.OverrideOnly
  open fun renderEmptyItemStack(slot: Int, player: Player): ItemStack = DEFAULT_EMPTY_ITEM_STACK

  /**
   * Override this if we need change AIR in guis to other item.
   *
   * @param slot slot index
   * @param player player instance
   * @param clickType type of click action
   * @param result result for action
   */
  @ApiStatus.OverrideOnly
  protected open fun isCancelNullItemExecution(
    player: Player,
    slot: Int,
    clickType: ClickType,
    result: InventoryConditionResult,
  ): Boolean = true

  @ApiStatus.OverrideOnly
  protected open fun viewerAdded(player: Player) {
    // only for override
  }

  @ApiStatus.OverrideOnly
  protected open fun viewerRemoved(player: Player) {
    // only for override
  }

  private fun isViewer(player: Player): Boolean = viewers.containsKey(player.uuid)

  /**
   *
   *
   * <h2>Set [GuiItem] to slot (if null = clearItem(slot)). And redraw this slot for all
   * viewers</h2>
   */
  protected fun setItem(slot: Int, guiItem: GuiItem?) {
    if (guiItem == null) {
      clearItem(slot)
      return
    }

    setItemNotNullUnsafe(slot, guiItem)
  }

  private fun setItemNotNullUnsafe(slot: Int, guiItem: GuiItem) {
    Check.notNull(guiItem, "guiItem can't be null!")

    val allow = guiItem.attached(this, slot)
    if (allow) {
      notifyCurrentItemBeDetachedUnsafe(slot)

      items[slot] = guiItem
      redrawSlotForAll(slot)
    }
  }

  /** Clear GuiItem in slot  */
  private fun clearItem(slot: Int) {
    notifyCurrentItemBeDetachedUnsafe(slot)
    items[slot] = null
    redrawSlotForAll(slot)
  }

  private fun notifyCurrentItemBeDetachedUnsafe(slot: Int) {
    val oldest = getItem(slot)
    oldest?.detached(this, slot)
  }

  /** Get [GuiItem] from slot  */
  private fun getItem(slot: Int): GuiItem? = items[slot]

  private val guiManager: GuiManager
    get() = GuiManager.get()

  companion object {
    @JvmStatic
    protected val DEFAULT_EMPTY_ITEM_STACK: ItemStack = ItemStack.AIR
    private val LOGGER = createLogger<GUI>()
  }
}
