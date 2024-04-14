package com.daylifecraft.minigames.command.debug

import com.daylifecraft.common.logging.building.LogBuilder
import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.logging.foundation.LogEvent
import com.daylifecraft.minigames.command.CommandsManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandData

abstract class AbstractDebugCommand protected constructor(
  command: String,
  vararg aliases: String,
) : Command(command, *aliases) {
  init {
    addSyntax(::onExecute)
  }

  protected fun onExecute(sender: CommandSender, context: CommandContext) {
    try {
      onCommandUse(sender, context)
    } catch (e: Exception) {
      sendMessage(sender, "debug.command.fail.general", context)
      LOGGER.build(LogEvent.GENERAL_DEBUG) {
        message("Cannot execute debug command")
        detailsSection(LogBuilder.KEY_ADDITIONAL, "stackTrace", e.stackTrace)
        detailsSection(LogBuilder.KEY_ADDITIONAL, "throwable", e.message)
      }
    }
  }

  protected abstract fun onCommandUse(sender: CommandSender, context: CommandContext)

  protected fun sendMessage(
    sender: CommandSender,
    string: String,
    context: CommandContext,
  ) {
    context.returnData =
      CommandData().also { returnData ->
        try {
          val language = CommandsManager.getSenderLanguage(sender)
          language.sendMiniMessage(string)
          returnData["value"] = string
        } catch (e: Exception) {
          LOGGER.build(LogEvent.GENERAL_DEBUG) {
            message("command-server-execute")
            detailsSection(LogBuilder.KEY_ADDITIONAL, "stackTrace", e.stackTrace)
            detailsSection(LogBuilder.KEY_ADDITIONAL, "throwable", e.message)
          }
          returnData["value"] = "$string.S"
        }
      }
  }

  companion object {
    private val LOGGER = createLogger<AbstractDebugCommand>()
  }
}
