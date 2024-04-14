package com.daylifecraft.minigames.command.punishment

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.Init.stopServerWithError
import com.daylifecraft.minigames.PermissionManager
import com.daylifecraft.minigames.command.AbstractPermissionCommand
import com.daylifecraft.minigames.command.CommandsManager.getSenderLanguage
import com.daylifecraft.minigames.command.CommandsManager.getText
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.profile.punishment.PunishmentProfile
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class PunishmentSetNoteCommand internal constructor() :
  AbstractPermissionCommand("set-note"),
  SubCommand {
  override val permission = "punishments.notes.set"

  private val idArgument = ArgumentType.String("id")
  private val noteArgument = ArgumentType.StringArray("note")

  init {
    addSyntax(this::onExecute, idArgument, noteArgument)
  }

  private fun onExecute(sender: CommandSender, context: CommandContext) {
    val id = context[idArgument]

    val profile: PunishmentProfile?

    try {
      // Get punishment profile from database by id
      profile = DatabaseManager.getPunishmentProfile(id)
    } catch (e: IllegalArgumentException) {
      stopServerWithError(e)
    }

    if (profile == null) {
      // TODO Could not find punishment profile
      return
    }

    // Check weather sender has permission
    if (sender is Player &&
      !PermissionManager.hasPermission(sender, "punishments.notes.set", profile.type)
    ) {
      return
    }

    val noteArray = context[noteArgument]

    val note = getText(noteArray)

    // Set internal note to punishment
    profile.setInternalNote(note)

    val language = getSenderLanguage(sender)

    sender.sendMessage(
      language.miniMessage(
        "punishments.internal-note-set.success",
        "punishmentId" to id,
        "punishmentType" to language.string("punishments.details.punishment-type-" + profile["type"]),
      ),
    )
  }
}
