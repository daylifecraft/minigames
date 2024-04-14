package com.daylifecraft.minigames.command.group.invites

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.condition.CommandCondition
import net.minestom.server.entity.Player

private const val INVITE_ID_ARGUMENT = "inviteId"

class GroupAcceptCommand :
  Command("accept"),
  SubCommand {
  private val inviteIdArgument = ArgumentType.Long(INVITE_ID_ARGUMENT)

  init {
    addSyntax(this::onExecute, inviteIdArgument)

    condition =
      CommandCondition { sender, _ ->
        sender is Player && groupInvitesMap.values.any { it.target.uuid == sender.uuid }
      }
  }

  private fun onExecute(sender: CommandSender, context: CommandContext) {
    val senderUUID = CommandsManager.getUuidFromSender(sender)
    val groupInvite = groupInvitesMap[context[inviteIdArgument]]

    if (groupInvite == null || groupInvite.target.uuid != senderUUID) {
      return
    }

    groupInvite.onAccept()
  }

  companion object {
    private val groupInvitesMap = mutableMapOf<Long, GroupInvite>()

    fun addGroupInvite(inviteId: Long, groupInvite: GroupInvite) {
      groupInvitesMap[inviteId] = groupInvite

      PlayerLanguage.get(groupInvite.target)
        .sendMiniMessage(
          "group.invite.received",
          "player" to groupInvite.inviter.username,
          INVITE_ID_ARGUMENT to inviteId.toString(),
        )
    }

    fun removeGroupInvite(id: Long) {
      groupInvitesMap.remove(id)
    }
  }
}
