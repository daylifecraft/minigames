package com.daylifecraft.minigames.instance.instances.lobby.gui

import com.daylifecraft.common.text.HardcodedText
import com.daylifecraft.common.text.PlayerText
import com.daylifecraft.common.text.i18n.DynamicPlayerText
import com.daylifecraft.minigames.command.group.GroupKickCommand.Companion.kick
import com.daylifecraft.minigames.command.group.GroupLeaveCommand.Companion.leave
import com.daylifecraft.minigames.database.DatabaseManager.getPlayerProfile
import com.daylifecraft.minigames.gui.GuiItem
import com.daylifecraft.minigames.profile.GuiControl.isPlayerOnlineInServer
import com.daylifecraft.minigames.profile.group.PlayersGroup
import com.daylifecraft.minigames.profile.group.PlayersGroupManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import com.daylifecraft.minigames.text.i18n.TranslateText
import com.daylifecraft.minigames.util.GuiUtil.StaticUsernameSkullOwnerPreRenderer
import com.daylifecraft.minigames.util.GuiUtil.isChest6SlotInEdge
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.UUID

private const val CHECK_GROUP_CACHE_PERIOD = 5
private const val REDRAW_PERIOD = 5

class GroupManageGui private constructor(
  player: Player,
  private val group: PlayersGroup,
) : AbstractListGui(player, TranslateText("menu.group.title")) {
  private var groupCacheHashCode: Int
  private var tickLimiter: Long = 0

  init {
    groupCacheHashCode = group.hashCode()
  }

  override fun tick(player: Player) {
    super.tick(player)
    if (!group.isBind || group.playerLeaderUUID != player.uuid
    ) {
      close()
    } else {
      if (tickLimiter % CHECK_GROUP_CACHE_PERIOD == 0L) {
        // TODO review. This is probably not working as expected
        val hashCode = group.hashCode()
        if (groupCacheHashCode != hashCode) {
          groupCacheHashCode = hashCode
          redrawList()
        }
      }

      if (tickLimiter % REDRAW_PERIOD == 0L) {
        redrawList()
      }
    }
    tickLimiter++
  }

  private fun setup() {
    setupAbstractListGui()
  }

  override fun renderEmptyItemStack(slot: Int, player: Player): ItemStack {
    if (isChest6SlotInEdge(slot)) {
      return BACKGROUND_R5
    }
    return super.renderEmptyItemStack(slot, player)
  }

  override val listSize: Int
    get() {
      val playerGroup = PlayersGroupManager.getGroupByPlayer(player) ?: return 0
      return playerGroup.groupSize
    }

  override val list: Array<GuiItem>
    get() {
      val ret: MutableList<GuiItem> = ArrayList()
      ret.add(GroupMemberGuiItem(group.playerLeaderUUID))

      for (uuid in group.getAllPlayersUUIDs()) {
        if (uuid === group.playerLeaderUUID) {
          continue
        }
        val item = GroupMemberGuiItem(uuid)
        ret.add(item)
      }

      return ret.toTypedArray<GuiItem>()
    }

  private inner class GroupMemberGuiItem(uuid: UUID?) : GuiItem() {
    private val playerProfile = getPlayerProfile("uuid", uuid.toString())

    init {
      if (playerProfile == null) {
        throw UnsupportedOperationException("playerProfile is null...")
      }

      material = Material.PLAYER_HEAD
      setPreRenderer(StaticUsernameSkullOwnerPreRenderer(playerProfile.username))
      text = HardcodedText(playerProfile.username)

      if (isLeaderHead) {
        lore = TranslateText("menu.group.leader.description")
      } else {
        lore =
          TranslateText(
            "menu.group.player.description",
            "targetOnlineStatus" to
              DynamicPlayerText {
                if (isPlayerOnlineInServer(playerProfile)) {
                  TRANSLATABLE_PLAYER_ONLINE
                } else {
                  TRANSLATABLE_PLAYER_OFFLINE
                }
              },
          )
      }
    }

    private val isLeaderHead: Boolean
      get() = group.playerLeaderUUID.toString() == playerProfile!!.uuid

    override fun playerClicked(
      item: GuiItem,
      player: Player,
      slot: Int,
      clickType: ClickType,
    ): Boolean {
      if (clickType == ClickType.DROP) {
        if (isLeaderHead) {
          leave(player, group, PlayerLanguage.get(player))
        } else {
          val username = playerProfile!!.username
          val uuid = UUID.fromString(playerProfile.uuid)
          kick(player, username, uuid, group, PlayerLanguage.get(player))
        }
      }

      return true
    }
  }

  companion object {
    private val TRANSLATABLE_PLAYER_ONLINE: PlayerText =
      TranslateText("menu.player-settings.online-toggler.description.online")
    private val TRANSLATABLE_PLAYER_OFFLINE: PlayerText =
      TranslateText("menu.player-settings.online-toggler.description.offline")
    private val BACKGROUND_R5 = ItemStack.of(Material.BLUE_STAINED_GLASS_PANE).withDisplayName(Component.empty())

    /**
     * try to show GroupManageGui (if fail = nothing...)
     *
     * @param player player to try
     */
    @JvmStatic
    fun showGroupManageGui(player: Player) {
      val playerGroup = PlayersGroupManager.getGroupByPlayer(player)
      if (playerGroup == null ||
        !playerGroup.isBind ||
        playerGroup.playerLeaderUUID != player.uuid
      ) {
        return
      }

      val gui = GroupManageGui(player, playerGroup)
      gui.setup()
      gui.show()
    }
  }
}
