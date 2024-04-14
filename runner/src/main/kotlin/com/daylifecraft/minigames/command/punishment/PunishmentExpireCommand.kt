package com.daylifecraft.minigames.command.punishment

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.common.util.extensions.miniMessage
import com.daylifecraft.minigames.PermissionManager
import com.daylifecraft.minigames.command.AbstractPermissionCommand
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.confirm.ConfirmableCommand
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.profile.punishment.PunishmentProfile
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class PunishmentExpireCommand :
  AbstractPermissionCommand("force-expire"),
  ConfirmableCommand,
  SubCommand {
  override val permission = "punishments.expire"

  private val idArgument = ArgumentType.String("id")

  init {
    addSyntax(this::onExecute, idArgument)
  }

  /**
   * This method will be called when the command was confirmed
   *
   * @param sender who send command
   * @param context command context
   */
  override fun onConfirm(sender: CommandSender, context: CommandContext) {
    val senderUuid = CommandsManager.getUuidFromSender(sender)

    // Get sender`s language
    val language =
      PlayerLanguage.get(
        MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(senderUuid)!!,
      )

    var profile: PunishmentProfile? = null

    val id = context[idArgument]
    try {
      // Get punishment profile from database by id
      profile = DatabaseManager.getPunishmentProfile(id)
    } catch (_: IllegalArgumentException) {
      language.sendMiniMessage("punishments.force-expire.fail-not-found")
    }

    if (profile == null) {
      language.sendMiniMessage("punishments.force-expire.fail-not-found")
      return
    }

    // Check weather sender has permission
    if (sender is Player &&
      !PermissionManager.hasPermission(sender, "punishments.expire.ban", profile.type)
    ) {
      return
    }

    // Force expire punishment
    val isExpired = profile.forceExpire()

    val message =
      if (isExpired) {
        language.string(
          "punishments.force-expire.success",
          "punishmentId" to id,
          "punishmentType" to language.string("punishments.details.punishment-type-" + profile["type"]),
        )
      } else {
        language.string("punishments.force-expire.fail", "punishmentId" to id)
      }

    // Send message to player
    sender.sendMessage(message.miniMessage())
  }

  private fun onExecute(sender: CommandSender, context: CommandContext) {
    showConfirm(sender, context)
  }
}
