package com.daylifecraft.minigames.command

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.command.PlayerFindParameter.Companion.get
import com.daylifecraft.minigames.command.confirm.ConfirmCommand
import com.daylifecraft.minigames.command.confirm.ConfirmRequest
import com.daylifecraft.minigames.command.confirm.ConfirmableCommand
import com.daylifecraft.minigames.command.debug.DebugCommand
import com.daylifecraft.minigames.command.friends.FriendsCommand
import com.daylifecraft.minigames.command.group.GroupCommand
import com.daylifecraft.minigames.command.group.GroupMessageCommand
import com.daylifecraft.minigames.command.kick.KickCommand
import com.daylifecraft.minigames.command.messages.PrivateMessageCommand
import com.daylifecraft.minigames.command.punishment.PunishmentCommand
import com.daylifecraft.minigames.command.punishment.give.ban.BanCommand
import com.daylifecraft.minigames.command.punishment.give.mute.MuteCommand
import com.daylifecraft.minigames.command.rounds.RoundsCommand
import com.daylifecraft.minigames.text.i18n.Language
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import com.daylifecraft.minigames.text.i18n.SenderLanguage
import net.minestom.server.command.CommandManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.entity.Player
import java.util.UUID
import kotlin.collections.set

object CommandsManager {
  private val COMMANDS: List<Class<out Command>> =
    listOf(
      BanCommand::class.java,
      MuteCommand::class.java,
      ConfirmCommand::class.java,
      FriendsCommand::class.java,
      GroupMessageCommand::class.java,
      GroupCommand::class.java,
      KickCommand::class.java,
      PunishmentCommand::class.java,
      PrivateMessageCommand::class.java,
      DebugCommand::class.java,
      RoundsCommand::class.java,
    )

  private val confirmRequests = HashMap<UUID, ConfirmRequest>()

  /**
   * Load commands into manager
   *
   * @param commandManager manager for commands
   * @param debugMode debug mod
   */
  fun load(commandManager: CommandManager, debugMode: Boolean) {
    for (commandClass in COMMANDS) {
      try {
        // Get command class
        val command = commandClass.getConstructor().newInstance()
        // Check weather is command a subcommand
        if (command !is SubCommand &&
          !(!debugMode && (command is DebugCommand))
        ) {
          // Register command if it is not sub command
          commandManager.register(command)
        }
      } catch (e: Exception) {
        throw IllegalArgumentException("Exception while load command ${commandClass.name}", e)
      }
    }
  }

  /**
   * Create confirm request for command
   *
   * @param command command
   * @param sender command sender
   * @param context command context
   */
  fun createConfirmRequest(
    command: ConfirmableCommand,
    sender: CommandSender,
    context: CommandContext,
  ): UUID {
    // Create new confirm request
    val request = ConfirmRequest(command, sender, context)

    // Get random id for request
    val id = UUID.randomUUID()

    // Put new confirm request
    confirmRequests[id] = request

    return id
  }

  /**
   * Delete confirm request by id
   *
   * @param id request uuid
   */
  fun deleteConfirmRequest(id: UUID) {
    if (!confirmRequests.containsKey(id)) {
      return
    }

    // Remove confirm request
    confirmRequests.remove(id)
  }

  /**
   * Gets confirm request by id
   *
   * @param id request uuid
   * @return request
   */
  fun getConfirmRequest(id: UUID): ConfirmRequest? = confirmRequests[id]

  /**
   * Gets sender uuid
   *
   * @param sender who send command
   * @return uuid for sender
   */
  fun getUuidFromSender(sender: CommandSender): UUID = if (sender is Player) {
    sender.uuid
  } else {
    UUID(0, 0)
  }

  /**
   * Gets sender language
   *
   * @param sender who send command
   * @return Player language
   */
  fun getSenderLanguage(sender: CommandSender): Language = if (sender is Player) {
    PlayerLanguage.get(sender)
  } else {
    SenderLanguage.get(sender)
  }

  /**
   * Returns player UUID from string: {Username/UUID}
   *
   * @param playerInputData Username or UUID string
   * @param onParseFailed that calls when input string does not match any pattern
   * @return Player UUID or null, if parse failed
   */
  fun parseUuidOrUserNameToUuid(playerInputData: String?, onParseFailed: Runnable = Runnable {}): UUID? {
    val findParameter = get(playerInputData!!)
    return when (findParameter) {
      PlayerFindParameter.USERNAME -> {
        PlayerManager.getPlayerUuid(playerInputData)
      }

      PlayerFindParameter.UUID -> {
        UUID.fromString(playerInputData)
      }

      else -> {
        onParseFailed.run()
        return null
      }
    }
  }

  fun getText(textArray: Array<String>): String = textArray.joinToString(" ")
}
