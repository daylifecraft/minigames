package com.daylifecraft.minigames.command.group

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.common.text.i18n.SharedKeys
import com.daylifecraft.minigames.profile.group.PlayersGroup
import com.daylifecraft.minigames.profile.group.PlayersGroupManager
import com.daylifecraft.minigames.profile.player.PlayerProfile
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.entity.Player

class GroupLeaveCommand :
  AbstractGroupCommand("leave"),
  SubCommand {
  init {
    isGroupRequired = true

    addSyntax()
  }

  /**
   * This method will be called when the command is used
   *
   * @param sender who send command
   * @param context command context
   * @param senderGroup group
   * @param playerLanguage language
   */
  public override fun onCommandUse(
    sender: Player,
    context: CommandContext,
    senderGroup: PlayersGroup?,
    playerLanguage: PlayerLanguage,
  ) {
    leave(sender, senderGroup!!, playerLanguage)
  }

  companion object {
    /**
     * Kick player from group
     *
     * @param executor who send command
     * @param playersGroup a group to leave
     * @param playerLanguage language
     */
    fun leave(
      executor: Player,
      playersGroup: PlayersGroup,
      playerLanguage: PlayerLanguage,
    ) {
      // If there are two players in the group, then after the leave of one it needs to be
      // disbanded
      if (playersGroup.groupSize <= 2) {
        playersGroup.removePlayer(executor.uuid)
        playerLanguage.sendMiniMessage("group.leave")
        PlayersGroupManager.sendMessageToPlayerGroup(
          playersGroup,
          "group.left-group",
          SharedKeys.PLAYER to executor.username,
        )

        // Disband the group
        PlayersGroupManager.removeGroup(playersGroup)
        return
      }

      val isLeader = executor.uuid == playersGroup.playerLeaderUUID

      playerLanguage.sendMiniMessage("group.leave")
      playersGroup.removePlayer(executor.uuid)

      PlayersGroupManager.sendMessageToPlayerGroup(
        playersGroup,
        "group.left-group",
        SharedKeys.PLAYER to executor.username,
      )

      // If a leader leaves the group, it is necessary to appoint another player as the leader
      if (isLeader) {
        // Getting next leader (By a time: the most old)
        // TODO Maybe we should do something when leader is the last leaved member (or ensure that that cannot happen)
        playersGroup.nextLeaderUUID?.let { nextLeaderUUID ->

          playersGroup.playerLeaderUUID = nextLeaderUUID

          PlayersGroupManager.sendMessageToPlayerGroup(
            playersGroup,
            "group.new-leader",
            SharedKeys.PLAYER to PlayerProfile.getUsername(nextLeaderUUID),
          )
        }
      }
    }
  }
}
