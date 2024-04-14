package com.daylifecraft.minigames.instance.instances.lobby.gui

import com.daylifecraft.common.text.PlayerText
import com.daylifecraft.common.util.ceilDiv
import com.daylifecraft.minigames.gui.BindingGUI
import com.daylifecraft.minigames.gui.GuiItem
import com.daylifecraft.minigames.text.i18n.TranslateText
import com.daylifecraft.minigames.util.GuiUtil
import com.daylifecraft.minigames.util.GuiUtil.isLR
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import org.jetbrains.annotations.Range

private const val PAGE_SIZE = 3 * 7
private const val LIST_START_SLOT = 10
private const val LAST_SLOT_IN_FIRST_ROW = 16
private const val LAST_SLOT_IN_SECOND_ROW = 25

private const val PREV_PAGE_BUTTON_SLOT = 39
private const val CURR_PAGE_BUTTON_SLOT = 40
private const val NEXT_PAGE_BUTTON_SLOT = 41

abstract class AbstractListGui internal constructor(
  player: Player,
  title: PlayerText,
) : BindingGUI(player, InventoryType.CHEST_6_ROW, title) {
  var page = 1
    private set

  protected abstract val listSize: Int

  protected abstract val list: Array<GuiItem>

  private fun getSlotForItem(index: Int): Int {
    var i = index % PAGE_SIZE + LIST_START_SLOT

    if (i > LAST_SLOT_IN_FIRST_ROW) {
      i += 2
    }

    if (i > LAST_SLOT_IN_SECOND_ROW) {
      i += 2
    }

    return i
  }

  private val lastPage: Int
    get() = listSize ceilDiv PAGE_SIZE

  private val isOnePage: Boolean
    get() = lastPage <= 1

  private fun setPage(page: Int) {
    this.page = page
    if (this.page <= 0) {
      this.page = 1
    }
    val last: Int
    if (this.page > (lastPage.also { last = it })) {
      this.page = last
    }
    redrawList()
    redrawControls()
  }

  private fun redrawControls() {
    redrawSlotForAll(PREV_PAGE_BUTTON_SLOT)
    redrawSlotForAll(CURR_PAGE_BUTTON_SLOT)
    redrawSlotForAll(NEXT_PAGE_BUTTON_SLOT)
  }

  fun setupAbstractListGui() {
    setItem(
      slot = 37,
      GuiUtil.ReusableElements.GLOBAL_GUI_ENDER_PEARL,
    )
    setItem(
      slot = PREV_PAGE_BUTTON_SLOT,
      UsageToPageButton(
        Material.REDSTONE_BLOCK,
        "menu.reusable.left-page-button.name",
        "previousPage",
        -1,
      ),
    )
    setItem(
      slot = CURR_PAGE_BUTTON_SLOT,
      CurrentPageIndicator("menu.reusable.current-page.name", "currentPage"),
    )
    setItem(
      slot = NEXT_PAGE_BUTTON_SLOT,
      UsageToPageButton(
        Material.EMERALD_BLOCK,
        "menu.reusable.right-page-button.name",
        "nextPage",
        1,
      ),
    )
    redrawList()
  }

  fun redrawList() {
    clearRedrawList()

    var i = ((page - 1) * PAGE_SIZE)
    while (i < listSize) {
      if (i >= (page * PAGE_SIZE)) {
        return
      }

      val guiItem = list[i]
      val slot:
        @Range(from = 10, to = 34)
        Int = getSlotForItem(i)

      setItem(slot, guiItem)
      i++
    }
  }

  private fun clearRedrawList() {
    for (i in 0 until PAGE_SIZE) {
      setItem(getSlotForItem(i), null)
    }
  }

  private inner class CurrentPageIndicator(private val titleKey: String, private val varKey: String) : GuiItem() {
    init {
      material = Material.SUNFLOWER
      text = generateText()
    }

    override fun renderForPlayer(player: Player): ItemStack {
      if (isOnePage) {
        return DEFAULT_EMPTY_ITEM_STACK
      }

      text = generateText()
      return super.renderForPlayer(player)
    }

    private fun generateText(): PlayerText = TranslateText(
      titleKey,
      varKey to page.toString(),
    )
  }

  private abstract inner class GoToPageButton(
    material: Material,
    private val textKey: String,
    private val varKey: String,
    private val shift: Int,
  ) : GuiItem() {
    init {
      super.material = material
    }

    fun generateTitle(): PlayerText = TranslateText(
      textKey,
      varKey to (page + shift).toString(),
    )

    val isBlocked: Boolean
      get() = page + shift == 0 || page + shift > lastPage

    override fun playerClicked(
      item: GuiItem,
      player: Player,
      slot: Int,
      clickType: ClickType,
    ): Boolean {
      if (clickType.isLR && !isBlocked) {
        setPage(page + shift)
      }
      return true
    }
  }

  private inner class UsageToPageButton(material: Material, textKey: String, varKey: String, shift: Int) : GoToPageButton(material, textKey, varKey, shift) {
    init {
      text = generateTitle()
    }

    override fun renderForPlayer(player: Player): ItemStack {
      if (isBlocked) {
        return DEFAULT_EMPTY_ITEM_STACK
      }
      text = generateTitle()
      return super.renderForPlayer(player)
    }
  }
}
