package com.daylifecraft.minigames.command.kick

import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.command.AbstractPermissionCommand
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.confirm.ConfirmableCommand
import com.daylifecraft.minigames.text.i18n.PlayerLanguage.Companion.get
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType

class KickCommand :
  AbstractPermissionCommand("kick"),
  ConfirmableCommand {
  override val permission = "punishments.kick"

  private val targetArgument = ArgumentType.String("target")
  private val reasonArgument = ArgumentType.StringArray("reason")

  init {
    addSyntax(this::onExecute, targetArgument)
    addSyntax(this::onExecute, targetArgument, reasonArgument)
  }

  private fun onExecute(sender: CommandSender, context: CommandContext) {
    showConfirm(sender, context)
  }

  /**
   * This method will be called when the command was confirmed
   *
   * @param sender who send command
   * @param context command context
   */
  override fun onConfirm(sender: CommandSender, context: CommandContext) {
    // Get sender`s language
    val language =
      get(
        MinecraftServer.getConnectionManager()
          .getOnlinePlayerByUuid(CommandsManager.getUuidFromSender(sender))!!,
      )

    val target = context[targetArgument]
    val targetUuid = CommandsManager.parseUuidOrUserNameToUuid(target)
    if (targetUuid == null) {
      language.sendMiniMessage("commands.reusable.fails.wrong-player")
      return
    }

    val targetPlayer =
      MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(targetUuid)
    if (targetPlayer == null) {
      language.sendMiniMessage(
        "commands.reusable.fails.target-player-offline",
        TARGET_PLAYER_VARIABLE to target,
      )
      return
    }

    val reasonArray = context[reasonArgument]
    if (reasonArray == null) {
      PlayerManager.kickPlayer(targetPlayer, language.string("kick.default"))
      language.sendMiniMessage(
        "punishments.kick.success.default-reason",
        TARGET_PLAYER_VARIABLE to targetPlayer.username,
      )
    } else {
      PlayerManager.kickPlayer(targetPlayer, CommandsManager.getText(reasonArray))
      language.sendMiniMessage(
        "punishments.kick.success",
        TARGET_PLAYER_VARIABLE to targetPlayer.username,
        REASON_VARIABLE to CommandsManager.getText(reasonArray),
      )
    }
  }

  companion object {
    private const val TARGET_PLAYER_VARIABLE = "targetPlayer"
    private const val REASON_VARIABLE = "reason"
  }
}
