package com.daylifecraft.minigames.command.punishment

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.minigames.PermissionManager
import com.daylifecraft.minigames.command.AbstractPermissionCommand
import com.daylifecraft.minigames.command.CommandsManager.getSenderLanguage
import com.daylifecraft.minigames.database.DatabaseManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class PunishmentViewDetailsCommand internal constructor() :
  AbstractPermissionCommand("view"),
  SubCommand {
  override val permission = "punishments.view"

  private val idArgument = ArgumentType.String("id")

  init {
    addSyntax(this::onExecute, idArgument)
  }

  private fun onExecute(sender: CommandSender, context: CommandContext) {
    val id = context[idArgument]

    val profile = DatabaseManager.getPunishmentProfile(id)

    if (profile == null) {
      // TODO 11.10.2023 Could not find profile
      LOGGER.debug("Profile by id $id cannot be found!")
      return
    }

    // Check weather sender has permission
    if (sender is Player &&
      !PermissionManager.hasPermission(sender, "punishments.view", profile.type)
    ) {
      return
    }

    val language = getSenderLanguage(sender)

    // Set more profile`s details to show
    profile.setMoreDetails()
    profile.getDetails(getSenderLanguage(sender)).language = language

    // Send message with details to sender
    sender.sendMessage(profile.getDetails(getSenderLanguage(sender)).show())
  }

  companion object {
    private val LOGGER = createLogger<PunishmentViewDetailsCommand>()
  }
}
