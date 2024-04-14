package com.daylifecraft.minigames.instance.instances.lobby.gui

import com.daylifecraft.common.text.HardcodedText
import com.daylifecraft.minigames.Init.miniGamesSettingsManager
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.gui.GuiItem
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager.preparePlayerForRoundSearch
import com.daylifecraft.minigames.minigames.search.IRoundSearchProvider
import com.daylifecraft.minigames.minigames.search.PlayerGroupRoundSearchProvider
import com.daylifecraft.minigames.minigames.search.SinglePlayerRoundSearchProvider.Companion.defaultRoundSearchProvider
import com.daylifecraft.minigames.minigames.settings.GeneralGameSettings
import com.daylifecraft.minigames.profile.group.PlayersGroup
import com.daylifecraft.minigames.profile.group.PlayersGroupManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import com.daylifecraft.minigames.text.i18n.TranslateText
import com.daylifecraft.minigames.util.GuiUtil.isChest6SlotInEdge
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

/** GUI displays available mini-games.  */
class GamesListGui(player: Player?) : AbstractListGui(player!!, TranslateText("menu.games.selector.title")) {
  private fun setup() {
    setupAbstractListGui()
  }

  override fun renderEmptyItemStack(slot: Int, player: Player): ItemStack {
    if (isChest6SlotInEdge(slot)) {
      return BACKGROUND_R1
    }
    return super.renderEmptyItemStack(slot, player)
  }

  override val listSize: Int
    get() = miniGamesSettingsManager!!.publicLoadedMiniGamesCount

  override val list: Array<GuiItem>
    get() {
      val items: MutableList<GuiItem> = ArrayList()

      for (generalGameSettings in miniGamesSettingsManager!!.allLoadedMiniGamesSettings) {
        val item = MiniGameGuiItem(player, generalGameSettings)
        if (Material.AIR == item.material) {
          continue
        }
        items.add(item)
      }

      return items.toTypedArray<GuiItem>()
    }

  /** MiniGame item in GUI. Description and ~in future~ clickable item to join MiniGame, etc.  */
  class MiniGameGuiItem(viewer: Player, private val generalGameSettings: GeneralGameSettings?) : GuiItem() {
    init {
      if (generalGameSettings == null || !generalGameSettings.isPublic) {
        material = Material.AIR
        text = HardcodedText.EMPTY
      } else {
        material = generalGameSettings.guiBlock
        text = TranslateText(generalGameSettings.displayNameKey)

        lore =
          if (generalGameSettings.permission == null ||
            viewer.hasPermission(generalGameSettings.permission)
          ) {
            TranslateText(generalGameSettings.descriptionKey)
          } else {
            TranslateText("menu.games.selector.game.not-available")
          }
      }
    }

    override fun playerClicked(
      item: GuiItem,
      player: Player,
      slot: Int,
      clickType: ClickType,
    ): Boolean {
      if (generalGameSettings!!.permission != null &&
        !player.hasPermission(generalGameSettings.permission!!)
      ) {
        return true
      }

      val playerGroup = PlayersGroupManager.getGroupByPlayer(player)

      if (playerGroup != null && !playerGroup.isPlayerLeader(player)) {
        PlayerLanguage.get(player).sendMiniMessage("group.not-leader-fail")
        return true
      }

      preparePlayerForRoundSearch(
        player,
        generalGameSettings.name,
        getRoundSearchProviderForPlayer(player, playerGroup),
      )
      player.closeInventory()
      return true
    }

    private fun getRoundSearchProviderForPlayer(player: Player, playersGroup: PlayersGroup?): IRoundSearchProvider {
      if (playersGroup == null) {
        return defaultRoundSearchProvider
      }

      return PlayerGroupRoundSearchProvider(
        player,
        playersGroup.playersWithoutOwner
          .map { PlayerManager.getPlayerByUuid(it)!! },
        generalGameSettings!!,
      )
    }
  }

  companion object {
    private val BACKGROUND_R1 = ItemStack.of(Material.PURPLE_STAINED_GLASS_PANE).withDisplayName(Component.empty())

    /**
     * Show GUI to player.
     *
     * @param player Player instance
     */
    fun showGamesListGui(player: Player?) {
      val gamesListGui = GamesListGui(player)

      gamesListGui.setup()
      gamesListGui.show()
    }
  }
}
