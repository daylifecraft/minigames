package com.daylifecraft.minigames.instance.instances.lobby.gui

import com.daylifecraft.common.text.PlayerText
import com.daylifecraft.minigames.gui.BindingGUI
import com.daylifecraft.minigames.gui.GUI
import com.daylifecraft.minigames.gui.GuiItem
import com.daylifecraft.minigames.profile.GuiControl.getSettingsOnlineStatus
import com.daylifecraft.minigames.profile.GuiControl.isDisablePartyInvites
import com.daylifecraft.minigames.profile.GuiControl.isDisablePrivateMessages
import com.daylifecraft.minigames.profile.GuiControl.isHideGlobalChat
import com.daylifecraft.minigames.profile.GuiControl.isHidePlayers
import com.daylifecraft.minigames.profile.GuiControl.setDisablePartyInvites
import com.daylifecraft.minigames.profile.GuiControl.setDisablePrivateMessages
import com.daylifecraft.minigames.profile.GuiControl.setHideGlobalChat
import com.daylifecraft.minigames.profile.GuiControl.setHidePlayers
import com.daylifecraft.minigames.profile.GuiControl.setSettingsOnlineMode
import com.daylifecraft.minigames.profile.settings.OnlineStatus
import com.daylifecraft.minigames.text.i18n.TranslateText
import com.daylifecraft.minigames.util.GuiUtil
import com.daylifecraft.minigames.util.GuiUtil.isChest6SlotInEdge
import com.daylifecraft.minigames.util.GuiUtil.isLR
import it.unimi.dsi.fastutil.booleans.BooleanConsumer
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.function.BooleanSupplier
import java.util.function.Supplier

class PlayerSettingsGui private constructor(player: Player) : BindingGUI(player, InventoryType.CHEST_6_ROW, TranslateText("menu.player-settings.title")) {
  private fun setup() {
    setItem(
      slot = 37,
      GuiUtil.ReusableElements.GLOBAL_GUI_ENDER_PEARL,
    )
    setItem(
      slot = 43,
      OnlineModeGuiItem(),
    )

    setItem(
      slot = 10,
      OptionGuiItem(
        trueTitle = TranslateText("menu.player-settings.pm-toggler.name"),
        falseTitle = TranslateText("menu.player-settings.pm-toggler.name"),
        falseDescription = TranslateText("menu.player-settings.pm-toggler.description.false"),
        trueDescription = TranslateText("menu.player-settings.pm-toggler.description.true"),
        stateSupplier = { isDisablePrivateMessages(player) },
        applyConsumer = { b: Boolean -> setDisablePrivateMessages(player, b) },
      ),
    )

    setItem(
      slot = 12,
      OptionGuiItem(
        trueTitle = TranslateText("menu.player-settings.group-toggler.name"),
        falseTitle = TranslateText("menu.player-settings.group-toggler.name"),
        falseDescription = TranslateText("menu.player-settings.group-toggler.description.false"),
        trueDescription = TranslateText("menu.player-settings.group-toggler.description.true"),
        stateSupplier = { isDisablePartyInvites(player) },
        applyConsumer = { b: Boolean -> setDisablePartyInvites(player, b) },
      ),
    )

    setItem(
      slot = 14,
      OptionGuiItem(
        trueTitle = TranslateText("menu.player-settings.global-chat-toggler.name"),
        falseTitle = TranslateText("menu.player-settings.global-chat-toggler.name"),
        falseDescription = TranslateText("menu.player-settings.global-chat-toggler.description.false"),
        trueDescription = TranslateText("menu.player-settings.global-chat-toggler.description.true"),
        stateSupplier = { isHideGlobalChat(player) },
        applyConsumer = { b: Boolean -> setHideGlobalChat(player, b) },
      ),
    )

    setItem(
      slot = 16,
      OptionGuiItem(
        trueTitle = TranslateText("menu.player-settings.lobby-players-toggler.name"),
        falseTitle = TranslateText("menu.player-settings.lobby-players-toggler.name"),
        falseDescription = TranslateText("menu.player-settings.lobby-players-toggler.description.false"),
        trueDescription = TranslateText("menu.player-settings.lobby-players-toggler.description.true"),
        stateSupplier = { isHidePlayers(player) },
        applyConsumer = { b: Boolean -> setHidePlayers(player, b) },
      ),
    )
  }

  override fun renderEmptyItemStack(slot: Int, player: Player): ItemStack {
    // black on the edge
    val isInEdge = isChest6SlotInEdge(slot)

    if (isInEdge) {
      return BACKGROUND_R1
    }

    return super.renderEmptyItemStack(slot, player)
  }

  private class OptionGuiItem(
    private val trueTitle: PlayerText,
    private val falseTitle: PlayerText,
    private val falseDescription: PlayerText,
    private val trueDescription: PlayerText,
    private val stateSupplier: BooleanSupplier,
    private val applyConsumer: BooleanConsumer,
  ) : GuiItem() {
    private var attachedTo: GUI? = null
    private var slot = 0
    private var state = false

    override fun attached(gui: GUI, slot: Int): Boolean {
      attachedTo = gui
      this.slot = slot
      update(stateSupplier.asBoolean, true)

      return super.attached(gui, slot)
    }

    override fun playerClicked(
      item: GuiItem,
      player: Player,
      slot: Int,
      clickType: ClickType,
    ): Boolean {
      if (clickType.isLR) {
        update(!state, false)
      }
      return true
    }

    private fun update(state: Boolean, noApply: Boolean) {
      this.state = state

      material = materialByState
      text = if (state) trueTitle else falseTitle
      lore = if (state) trueDescription else falseDescription

      attachedTo!!.redrawSlotForAll(slot)

      if (!noApply) {
        applyConsumer.accept(state)
      }
    }

    private val materialByState: Material
      get() =
        if (state) {
          Material.RED_CANDLE
        } else {
          Material.GREEN_CANDLE
        }
  }

  /** online mode switcher  */
  inner class OnlineModeGuiItem internal constructor() : GuiItem() {
    private var slot = 0
    private var guiOnlineStatus: GuiOnlineStatus? = null

    init {
      text = TranslateText("menu.player-settings.online-toggler.name")
      lore =
        TranslateText(
          "menu.player-settings.online-toggler.description",
          "onlineStatus" to Supplier<PlayerText> { guiOnlineStatus?.playerText },
        )
    }

    override fun attached(gui: GUI, slot: Int): Boolean {
      this.slot = slot
      update(GuiOnlineStatus.ofStatus(getSettingsOnlineStatus(player)), true)

      return super.attached(gui, slot)
    }

    override fun playerClicked(
      item: GuiItem,
      player: Player,
      slot: Int,
      clickType: ClickType,
    ): Boolean {
      if (clickType.isLR) {
        update(guiOnlineStatus!!.next(), false)
      }
      return true
    }

    fun update(mode: GuiOnlineStatus, noApply: Boolean) {
      guiOnlineStatus = mode
      material = mode.material

      redrawSlotForAll(slot)
      if (!noApply) {
        setSettingsOnlineMode(player, guiOnlineStatus!!.status)
      }
    }
  }

  companion object {
    private val BACKGROUND_R1 = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE).withDisplayName(Component.empty())

    @JvmStatic
    fun showSettingsGui(player: Player) {
      val gui = PlayerSettingsGui(player)
      gui.setup()
      gui.show()
    }
  }
}

enum class GuiOnlineStatus(val status: OnlineStatus, val material: Material, langKey: String) {
  ONLINE(
    OnlineStatus.ONLINE,
    Material.GREEN_CONCRETE,
    "menu.player-settings.online-toggler.description.online",
  ),
  FRIENDS_ONLY(
    OnlineStatus.FRIENDS_ONLY,
    Material.BLUE_CONCRETE,
    "menu.player-settings.online-toggler.description.friends-only",
  ),
  OFFLINE(
    OnlineStatus.OFFLINE,
    Material.RED_CONCRETE,
    "menu.player-settings.online-toggler.description.offline",
  ),
  ;

  val playerText: PlayerText = TranslateText(langKey)

  fun next(): GuiOnlineStatus = when (this) {
    ONLINE -> FRIENDS_ONLY
    FRIENDS_ONLY -> OFFLINE
    OFFLINE -> ONLINE
  }

  fun previous(): GuiOnlineStatus = when (this) {
    OFFLINE -> FRIENDS_ONLY
    FRIENDS_ONLY -> ONLINE
    ONLINE -> OFFLINE
  }

  companion object {
    fun ofStatus(status: OnlineStatus): GuiOnlineStatus {
      for (value in entries) {
        if (status == value.status) {
          return value
        }
      }
      throw NoSuchElementException("Unknown online status: $status...")
    }
  }
}
