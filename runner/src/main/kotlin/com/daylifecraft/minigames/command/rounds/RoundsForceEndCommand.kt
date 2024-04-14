package com.daylifecraft.minigames.command.rounds

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.command.AbstractPermissionCommand
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.confirm.ConfirmableCommand
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import org.bson.types.ObjectId

/** Command ForceEnds round by id  */
class RoundsForceEndCommand internal constructor() :
  AbstractPermissionCommand("force-end"),
  ConfirmableCommand,
  SubCommand {

  override val permission = "rounds.force-end"

  private val idArgument = ArgumentType.String("id")

  init {
    addSyntax(this::onExecute)
    addSyntax(this::onExecute, idArgument)
  }

  /**
   * This method will be called when the command was confirmed
   *
   * @param sender who send command
   * @param context command context
   */
  override fun onConfirm(sender: CommandSender, context: CommandContext) {
    val playerLanguage = CommandsManager.getSenderLanguage(sender)
    if (!context.has(idArgument)) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.required-args-not-found")
      return
    }

    val idString = context[idArgument]
    if (!ObjectId.isValid(idString)) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.wrong-objectid")
      return
    }

    // val objectId = ObjectId(idString)

    // TODO 28.10.2023 Force End round
  }

  private fun onExecute(sender: CommandSender, context: CommandContext) {
    showConfirm(sender, context)
  }
}
