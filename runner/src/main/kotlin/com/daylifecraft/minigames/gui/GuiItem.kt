package com.daylifecraft.minigames.gui

import com.daylifecraft.common.text.PlayerText
import com.daylifecraft.common.util.extensions.miniMessage
import com.daylifecraft.minigames.gui.GuiItem.PlayerInteractionHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.entity.Player
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemMeta
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import org.jetbrains.annotations.ApiStatus

open class GuiItem {
  var amount: Int
  lateinit var material: Material

  protected var text: PlayerText? = null
  protected var onInteract: PlayerInteractionHandler? = null
  protected var lore: PlayerText? = null
  protected var preRenderer: PreRenderer? = null

  protected constructor() {
    // empty constructor (need init using setters or lateinits will throw)
    // TODO think about abstract getters
    amount = 1
  }

  constructor(
    material: Material,
    amount: Int,
    text: PlayerText,
    lore: PlayerText? = null,
    onInteract: PlayerInteractionHandler? = null,
  ) {
    this.material = material
    this.text = text
    this.lore = lore
    this.amount = amount
    this.onInteract = onInteract
  }

  // TODO Maybe we should create builder as a separate class
  fun setPreRenderer(preRenderer: PreRenderer?): GuiItem {
    this.preRenderer = preRenderer
    return this
  }

  open fun renderForPlayer(player: Player): ItemStack {
    val preRendered = preRenderer?.renderMaterial(material)
    val itemStackBuilder =
      ItemStack.builder(
        preRendered ?: material,
      ).amount(amount)

    text?.let { nonNullText ->
      itemStackBuilder.displayName(
        nonNullText.miniMessage(player).style(
          Style.empty().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
        ),
      )
    }

    lore?.let { nonNullLore ->
      val loreComponents: MutableList<Component> = ArrayList()
      val rawLoreLines = nonNullLore.string(player).split("\n")
      for (rawLoreLine in rawLoreLines) {
        loreComponents.add(
          rawLoreLine.miniMessage()
            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
        )
      }
      itemStackBuilder.lore(loreComponents)
    }

    preRenderer?.let { nonNullPrerender ->
      itemStackBuilder.meta { metaBuilder ->
        nonNullPrerender.preRenderItemStack(player, metaBuilder)
      }
    }

    return itemStackBuilder.build()
  }

  open fun playerClicked(
    item: GuiItem,
    player: Player,
    slot: Int,
    clickType: ClickType,
  ): Boolean = (onInteract ?: NO_INTERACTION).interact(item, player, slot, clickType)

  @ApiStatus.OverrideOnly
  open fun attached(gui: GUI, slot: Int): Boolean {
    // Override only
    return true
  }

  @ApiStatus.OverrideOnly
  open fun detached(gui: GUI, slot: Int) {
    // Override only
  }

  fun interface PlayerInteractionHandler {
    fun interact(
      item: GuiItem,
      player: Player,
      slot: Int,
      clickType: ClickType,
    ): Boolean
  }

  abstract class PreRenderer {
    open fun preRenderItemStack(player: Player, builder: ItemMeta.Builder) {
      // Override only
    }

    fun renderMaterial(material: Material?): Material? = material
  }

  /** PreRenderer adds "SkullOwner" tag to item (for PLAYER_HEAD material)  */
  class ViewerPlayerHeadSkinPreRenderer : PreRenderer() {
    override fun preRenderItemStack(player: Player, builder: ItemMeta.Builder) {
      builder[Tag.String("SkullOwner")] = player.username
    }
  }

  companion object {
    val NO_INTERACTION: PlayerInteractionHandler =
      PlayerInteractionHandler { _, _, _, _ -> true }
  }
}
