package com.daylifecraft.minigames.instance.instances.lobby.gui

import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.text.HardcodedText
import com.daylifecraft.minigames.command.group.invites.GroupInviteCommand.Companion.invite
import com.daylifecraft.minigames.database.DatabaseManager.getPlayerProfile
import com.daylifecraft.minigames.gui.GuiItem
import com.daylifecraft.minigames.instance.instances.lobby.gui.FriendRemoveConfirmationGui.Companion.showFriendRemoveConfirmationGui
import com.daylifecraft.minigames.profile.GuiControl.getFriendsCount
import com.daylifecraft.minigames.profile.GuiControl.getFriendsStringUuids
import com.daylifecraft.minigames.profile.GuiControl.getPlayerProfileByStringUuid
import com.daylifecraft.minigames.profile.GuiControl.isPlayerOnlineInServer
import com.daylifecraft.minigames.profile.group.PlayersGroupManager
import com.daylifecraft.minigames.profile.player.PlayerProfile
import com.daylifecraft.minigames.profile.settings.OnlineStatus
import com.daylifecraft.minigames.profile.settings.OnlineStatus.Companion.ofDBKey
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import com.daylifecraft.minigames.text.i18n.TranslateText
import com.daylifecraft.minigames.util.GuiUtil.StaticUsernameSkullOwnerPreRenderer
import com.daylifecraft.minigames.util.GuiUtil.isChest6SlotInEdge
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

private const val REDRAW_PERIOD = 40

class FriendsListGui private constructor(player: Player) : AbstractListGui(player, TranslateText("menu.friends-list.title")) {
  private var tickLimiter = 0

  override fun tick(player: Player) {
    super.tick(player)

    if (tickLimiter % REDRAW_PERIOD == 0) {
      redrawList()
    }
  }

  private fun setup() {
    setupAbstractListGui()
  }

  public override fun viewerAdded(player: Player) {
    redrawList()
  }

  override val listSize: Int
    get() = getFriendsCount(player)

  override val list: Array<GuiItem>
    get() {
      val list: MutableList<GuiItem> = ArrayList()
      for (friendUuid in getFriendsStringUuids(player)) {
        val profile = getPlayerProfileByStringUuid(friendUuid)
        if (profile == null) {
          LOGGER.debug(
            """
              WARNING! PLAYER IN FRIENDS NOT FOUND IN DATABASE!
              friendUuid=$friendUuid; player=${player.uuid}
            """.trimIndent(),
          )
        }
        val item = FriendGuiItem(profile)
        list.add(item)
      }
      return list.toTypedArray<GuiItem>()
    }

  override fun renderEmptyItemStack(slot: Int, player: Player): ItemStack {
    if (isChest6SlotInEdge(slot)) {
      return BACKGROUND_R1
    }
    return super.renderEmptyItemStack(slot, player)
  }

  inner class FriendGuiItem internal constructor(private val friendProfile: PlayerProfile?) : GuiItem() {
    init {
      if (friendProfile == null) {
        material = Material.AIR
        text = HardcodedText.EMPTY
      } else {
        material = Material.PLAYER_HEAD
        text = HardcodedText(friendProfile.username)
        lore =
          TranslateText(
            "menu.friends-list.player.description",
            "targetOnlineStatus" to playerOnlineStatus,
          )
        preRenderer = StaticUsernameSkullOwnerPreRenderer(friendProfile.username)
      }
    }

    private val playerOnlineStatus: String
      get() {
        val onlineStatusMode =
          ofDBKey(friendProfile!!.settings!!.onlineStatus)
        val isOnline = isPlayerOnlineInServer(friendProfile)
        val key: String
        val allowedShow =
          when (onlineStatusMode) {
            OnlineStatus.OFFLINE -> {
              false
            }

            OnlineStatus.FRIENDS_ONLY -> {
              getPlayerProfile(player)
                ?.getFriends()
                ?.contains(friendProfile.uuid) ?: false
            }

            else -> true
          }
        key =
          if (isOnline && allowedShow) {
            "menu.player-settings.online-toggler.description.online"
          } else {
            "menu.player-settings.online-toggler.description.offline"
          }
        return PlayerLanguage.get(player).string(key)
      }

    override fun playerClicked(
      item: GuiItem,
      player: Player,
      slot: Int,
      clickType: ClickType,
    ): Boolean {
      // if profile is null this item is break by database.
      if (friendProfile != null) {
        if (clickType == ClickType.DROP) {
          showFriendRemoveConfirmationGui(
            player,
            this@FriendsListGui,
            friendProfile,
          )
        }

        if (clickType == ClickType.LEFT_CLICK) {
          invite(
            player,
            MinecraftServer.getConnectionManager()
              .getOnlinePlayerByUsername(friendProfile.username),
            PlayersGroupManager.getGroupByPlayer(player),
            PlayerLanguage.get(player),
          )
        }
      }
      return true
    }
  }

  companion object {
    private val BACKGROUND_R1 = ItemStack.of(Material.GREEN_STAINED_GLASS_PANE).withDisplayName(Component.empty())
    private val LOGGER = createLogger<FriendsListGui>()

    fun showFriendsListGui(player: Player) {
      val friendsListGui = FriendsListGui(player)
      friendsListGui.setup()
      friendsListGui.show()
    }
  }
}
