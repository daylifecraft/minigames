package com.daylifecraft.minigames.command.rounds

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.command.AbstractPermissionCommand
import net.minestom.server.command.builder.arguments.ArgumentType

/**
 * Sends a message to all players in the round, if the round is initialized and not
 * completed/canceled, it is necessary to be in observation mode in the round
 */
class RoundsSayCommand internal constructor() :
  AbstractPermissionCommand("say"),
  SubCommand {
  override val permission = "rounds.say"

  private val textMessageArgument = ArgumentType.StringArray("textMessage")

  init {
    addSyntax({ _, _ -> }, textMessageArgument)
  }
}
