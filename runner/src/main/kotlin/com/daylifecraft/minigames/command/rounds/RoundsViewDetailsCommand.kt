package com.daylifecraft.minigames.command.rounds

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.command.AbstractPermissionCommand
import com.daylifecraft.minigames.command.CommandsManager.getSenderLanguage
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import org.bson.types.ObjectId

/** Displays details about the specified round in the chat  */
class RoundsViewDetailsCommand internal constructor() :
  AbstractPermissionCommand("view-details"),
  SubCommand {
  override val permission = "rounds.view-details"

  private val idArgument = ArgumentType.String("id")

  init {
    addSyntax(this::onExecute)
    addSyntax(this::onExecute, idArgument)
  }

  private fun onExecute(sender: CommandSender, context: CommandContext) {
    val playerLanguage = getSenderLanguage(sender)
    if (!context.has(idArgument)) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.required-args-not-found")
      return
    }
    val rawRoundId = context[idArgument]
    if (!ObjectId.isValid(rawRoundId)) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.wrong-objectid")
      return
    }
    // val roundId = ObjectId(rawRoundId)

    // TODO 28.10.2023 Send round details to player
  }
}
