package com.daylifecraft.minigames.instance.instances.lobby.gui

import com.daylifecraft.common.text.PlayerText
import com.daylifecraft.common.text.i18n.DynamicPlayerText
import com.daylifecraft.common.util.TimeUtil.formatSeconds
import com.daylifecraft.common.util.TimeUtil.getElapsedSecondsByTime
import com.daylifecraft.minigames.Init.miniGamesSettingsManager
import com.daylifecraft.minigames.gui.GUI
import com.daylifecraft.minigames.gui.GuiItem
import com.daylifecraft.minigames.gui.GuiItem.ViewerPlayerHeadSkinPreRenderer
import com.daylifecraft.minigames.instance.instances.lobby.gui.GroupManageGui.Companion.showGroupManageGui
import com.daylifecraft.minigames.instance.instances.lobby.gui.PlayerSettingsGui.Companion.showSettingsGui
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager.getMiniGameQueueElement
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager.getPlayerMiniGameLockData
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager.onPlayerRejectRoundSearch
import com.daylifecraft.minigames.profile.group.PlayersGroupManager
import com.daylifecraft.minigames.text.i18n.TranslateText
import com.daylifecraft.minigames.util.GuiUtil.isChest6SlotInEdge
import com.daylifecraft.minigames.util.GuiUtil.isLR
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class GlobalGui private constructor(private val viewer: Player) : GUI(InventoryType.CHEST_6_ROW, TranslateText("menu.global.title")) {
  private var tickCounter = 0

  private fun setup() {
    setItem(
      slot = 22,
      GuiItem(
        material = Material.PLAYER_HEAD,
        amount = 1,
        text = TranslateText("menu.global.player-settings.name"),
        lore = TranslateText("menu.global.player-settings.description"),
      ) { _, player: Player, _, clickType: ClickType ->
        if (clickType.isLR) {
          showSettingsGui(player)
        }
        true
      }.setPreRenderer(ViewerPlayerHeadSkinPreRenderer()),
    )

    setItem(
      slot = 20,
      GuiItem(
        material = Material.GREEN_BANNER,
        amount = 1,
        text = TranslateText("menu.global.player-friends.name"),
        lore = TranslateText("menu.global.player-friends.description"),
      ) { _, player: Player, _, clickType: ClickType ->
        if (clickType.isLR) {
          FriendsListGui.showFriendsListGui(player)
        }
        true
      },
    )

    setItem(
      slot = 24,
      GuiItem(
        material = Material.BLUE_BANNER,
        amount = 1,
        text = TranslateText("menu.global.group.name"),
        lore =
        DynamicPlayerText { player ->
          val group = PlayersGroupManager.getGroupByPlayer(player.uuid)
          val isInGroup = group != null
          var isLeader = false
          if (isInGroup) {
            isLeader = group!!.playerLeaderUUID == player.uuid
          }
          return@DynamicPlayerText if (isInGroup && isLeader) {
            TRANSLATE_LORE_GROUP
          } else {
            TRANSLATE_LORE_GROUP_NOT_IN_GROUP_OR_NOT_LEADER
          }
        },
      ) { _, player: Player, _, clickType: ClickType ->
        if (clickType.isLR) {
          showGroupManageGui(player)
        }
        true
      },
    )

    setItem(MINI_GAME_COMPASS_SLOT, MiniGamesGuiCompass(viewer))
  }

  override fun tick(player: Player) {
    super.tick(player)

    if (tickCounter % UPDATE_TICKS_COUNTS == 0) {
      setItem(MINI_GAME_COMPASS_SLOT, MiniGamesGuiCompass(player))
    }
    tickCounter++
  }

  override fun renderEmptyItemStack(slot: Int, player: Player): ItemStack {
    // black on the edge
    val isBlackBack = isChest6SlotInEdge(slot)

    if (isBlackBack) {
      return BACKGROUND_R1
    }
    return super.renderEmptyItemStack(slot, player)
  }

  internal class MiniGamesGuiCompass(viewer: Player) : GuiItem() {
    init {
      amount = 1

      val playerMiniGameLock =
        getPlayerMiniGameLockData(viewer.uuid)

      if (playerMiniGameLock != null) {
        val miniGameSettings = miniGamesSettingsManager.getGeneralGameSettings(
          playerMiniGameLock.miniGameId,
        )
        material = miniGameSettings!!.guiBlock
        text = TranslateText("menu.global.games-in-queue.name")
        lore = TranslateText(
          "menu.global.games-in-queue.description",
          "minigameDisplayName" to TranslateText(miniGameSettings.displayNameKey),
          "eTime" to formatSeconds(
            getElapsedSecondsByTime(playerMiniGameLock.startStageSeconds),
            TIME_FORMAT,
          ),
        )
      } else {
        material = Material.RECOVERY_COMPASS
        text = TranslateText("menu.global.games.name")
        lore = TranslateText("menu.global.games.description")
      }
    }

    override fun playerClicked(
      item: GuiItem,
      player: Player,
      slot: Int,
      clickType: ClickType,
    ): Boolean {
      val playerQueueData = getMiniGameQueueElement(player.uuid)

      when {
        !PlayerMiniGameManager.isPlayerLocked(player.uuid) && clickType.isLR -> {
          GamesListGui.showGamesListGui(player)
        }

        playerQueueData != null && playerQueueData.isNotStartingGame && clickType == ClickType.DROP -> {
          onPlayerRejectRoundSearch(player)
        }
      }

      return true
    }

    companion object {
      private const val TIME_FORMAT = "mm:ss"
    }
  }

  companion object {
    const val UPDATE_TICKS_COUNTS: Int = 20
    const val MINI_GAME_COMPASS_SLOT: Int = 40
    private val BACKGROUND_R1 = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE).withDisplayName(Component.empty())

    private val TRANSLATE_LORE_GROUP: PlayerText = TranslateText("menu.global.group.description")
    private val TRANSLATE_LORE_GROUP_NOT_IN_GROUP_OR_NOT_LEADER: PlayerText =
      TranslateText("menu.global.group.description.not-leader-or-not-in-group")

    fun showGuiToPlayer(player: Player) {
      val gui = GlobalGui(player)

      gui.setup()
      gui.show(player)
    }
  }
}
