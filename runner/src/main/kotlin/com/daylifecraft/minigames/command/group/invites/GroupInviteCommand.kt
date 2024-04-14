package com.daylifecraft.minigames.command.group.invites

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.common.finder.OnlinePlayerFinder
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.argument.ArgumentOnlinePlayers
import com.daylifecraft.minigames.command.group.AbstractGroupCommand
import com.daylifecraft.minigames.profile.group.PlayersGroup
import com.daylifecraft.minigames.profile.group.PlayersGroupManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player

class GroupInviteCommand :
  AbstractGroupCommand("invite"),
  SubCommand {
  init {
    val usernameArgument = ArgumentOnlinePlayers("username")
    usernameArgument.suggestion.excludeNotFriends(true)

    addSyntax(this::onExecute, usernameArgument)
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
    // Getting target username
    val finder: OnlinePlayerFinder = context["username"]

    if (finder.isPlayerIsNotOnline) {
      playerLanguage.sendMiniMessage("group.invite.sent.fail-incorrect")
      return
    }

    val playerToInvite = finder.player
    invite(sender, playerToInvite, senderGroup, playerLanguage)
  }

  companion object {
    private fun onInviteAccept(
      inviteId: Long,
      playerToInvite: Player,
      senderPlayer: Entity,
    ) {
      // Remove invite request
      GroupAcceptCommand.removeGroupInvite(inviteId)

      // If during the processing of the invitation the player has entered another group
      if (PlayersGroupManager.getGroupByPlayer(playerToInvite.uuid) != null) {
        return
      }

      // If sender group is null - create, otherwise leader check
      var playersGroup = PlayersGroupManager.getGroupByPlayer(senderPlayer.uuid)
      if (playersGroup == null) {
        playersGroup =
          PlayersGroupManager.createGroup(senderPlayer.uuid, playerToInvite.uuid)
      } else if (playersGroup.playerLeaderUUID == senderPlayer.uuid) {
        playersGroup.addPlayer(playerToInvite.uuid)
      } else {
        return
      }

      // Create group can return null, but this should not happen
      if (playersGroup == null) {
        return
      }

      PlayersGroupManager.sendMessageToPlayerGroup(
        playersGroup,
        "group.invite.accepted",
        "player" to playerToInvite.username,
      )
    }

    /**
     * Invite player into group
     *
     * @param senderPlayer who send command
     * @param playerToInvite who needs to invite
     * @param senderGroup group
     * @param playerLanguage language
     */
    fun invite(
      senderPlayer: Player,
      playerToInvite: Player?,
      senderGroup: PlayersGroup?,
      playerLanguage: PlayerLanguage,
    ) {
      // If the player is offline or has disabled invitations
      if (playerToInvite == null ||
        PlayerManager.loadPlayer(playerToInvite).settings!!.disablePartyInvites
      ) {
        playerLanguage.sendMiniMessage("group.invite.sent.fail")
        return
      }

      // If target player already in group
      if (PlayersGroupManager.getGroupByPlayer(playerToInvite.uuid) != null) {
        playerLanguage.sendMiniMessage(
          "group.invite.sent.fail-in-group",
          "targetPlayer" to playerToInvite.username,
        )
        return
      }

      if (senderGroup != null && senderGroup.playerLeaderUUID != senderPlayer.uuid) {
        playerLanguage.sendMiniMessage("group.not-leader-fail")
        return
      }

      // Send confirm action
      playerLanguage.sendMiniMessage(
        "group.invite.sent",
        "targetPlayer" to playerToInvite.username,
      )

      // Creating invite
      val inviteId = System.currentTimeMillis()
      GroupAcceptCommand.addGroupInvite(
        inviteId,
        GroupInvite(
          inviter = senderPlayer,
          target = playerToInvite,
          onAccept = {
            onInviteAccept(inviteId, playerToInvite, senderPlayer)
          },
        ),
      )
    }
  }
}
