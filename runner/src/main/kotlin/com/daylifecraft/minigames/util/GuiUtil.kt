package com.daylifecraft.minigames.util

import com.daylifecraft.minigames.gui.GuiItem
import com.daylifecraft.minigames.gui.GuiItem.PreRenderer
import com.daylifecraft.minigames.instance.instances.lobby.gui.GlobalGui
import com.daylifecraft.minigames.text.i18n.TranslateText
import net.minestom.server.entity.Player
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemMeta
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag

object GuiUtil {
  private const val SLOTS_IN_LINE = 9
  private const val LAST_COLUMN_X = 8
  private const val LAST_ROW_Y = 5

  // ONLY FOR InventoryType.CHEST_6_ROW
  fun isChest6SlotInEdge(slot: Int): Boolean {
    // coordinates from left top corner
    val x = slot % SLOTS_IN_LINE
    val y = slot / SLOTS_IN_LINE

    return x == 0 || x == LAST_COLUMN_X || y == 0 || y == LAST_ROW_Y
  }

  fun isChest6SlotInUpperRow(slot: Int): Boolean = (slot / SLOTS_IN_LINE) == 0

  fun isChest6SlotInLowerRow(slot: Int): Boolean = (slot / SLOTS_IN_LINE) == LAST_ROW_Y

  /** return true if clickType is LEFT_CLICK or RIGHT_CLICK  */
  val ClickType.isLR: Boolean
    get() = this == ClickType.LEFT_CLICK || this == ClickType.RIGHT_CLICK

  val ClickType.isNotLR: Boolean
    get() = this.isLR.not()

  object ReusableElements {
    /** langKey = "menu.reusable.return.name" action = open GlobalGui item = ENDER_PEARL  */
    val GLOBAL_GUI_ENDER_PEARL: GuiItem =
      GuiItem(
        Material.ENDER_PEARL,
        1,
        TranslateText("menu.reusable.return.name"),
        null,
      ) { _, player: Player, _, _ ->
        GlobalGui.showGuiToPlayer(player)
        true
      }
  }

  class StaticUsernameSkullOwnerPreRenderer(private val username: String) : PreRenderer() {
    override fun preRenderItemStack(player: Player, builder: ItemMeta.Builder) {
      builder[Tag.String("SkullOwner")] = username
    }
  }
}
