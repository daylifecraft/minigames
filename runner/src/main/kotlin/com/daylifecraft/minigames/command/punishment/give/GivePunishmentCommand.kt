package com.daylifecraft.minigames.command.punishment.give

import com.daylifecraft.minigames.command.AbstractPermissionCommand
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.PlayerFindParameter
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import java.time.Duration
import java.util.UUID

abstract class GivePunishmentCommand protected constructor(
  name: String,
  vararg aliases: String,
) : AbstractPermissionCommand(name, *aliases) {
  /**
   * This method will be called when the command is used
   *
   * @param sender who send command
   * @param context command context
   */
  fun execute(sender: CommandSender, context: CommandContext) {
    val reasonArray: Array<String>? = context["reason"]
    val playerString: String = context["player"] ?: return
    val duration: Duration? = context["duration"]

    // Get duration in seconds
    val durationSeconds = duration?.seconds ?: -1

    // Get uuid from sender
    val moderatorUUID = CommandsManager.getUuidFromSender(sender)

    val reason = CommandsManager.getText(reasonArray!!)

    // Get find parameter from sender`s input
    val findParameter = PlayerFindParameter.get(playerString)

    // Give punishment
    give(sender, findParameter, playerString, moderatorUUID, reason, durationSeconds)
  }

  protected abstract fun give(
    sender: CommandSender,
    findParameter: PlayerFindParameter,
    playerString: String,
    moderatorUUID: UUID,
    reason: String,
    duration: Long,
  )
}
