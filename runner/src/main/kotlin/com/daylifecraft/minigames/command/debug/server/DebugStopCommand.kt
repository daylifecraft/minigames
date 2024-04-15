package com.daylifecraft.minigames.command.debug.server

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.common.util.extensions.minestom.currentArgumentValue
import com.daylifecraft.minigames.ShutdownHook
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.utils.StringUtils

/** '/~ stop' command executor  */
class DebugStopCommand :
  AbstractDebugCommand("stop"),
  SubCommand {
  private val dryRunArgument = ArgumentType.String("dry-run")

  init {
    dryRunArgument.setSuggestionCallback { _, _, suggestion ->
      val input = suggestion.currentArgumentValue
      if (!input.contains(StringUtils.SPACE)) {
        suggestion.addEntry(SuggestionEntry(dryRunArgument.id))
      }
    }

    addSyntax(this::onExecute, dryRunArgument)
    addSyntax(this::onExecute)
  }

  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    val senderLanguage = CommandsManager.getSenderLanguage(sender)

    val hook = ShutdownHook.global

    if (context.has(dryRunArgument)) {
      senderLanguage.sendMiniMessage(SERVER_STOP_DRY)
      hook.run(1)
      return
    }
    senderLanguage.sendMiniMessage(SERVER_STOP)
    hook.run(0)
  }

  companion object {
    private const val SERVER_STOP = "debug.server-stop.success"
    private const val SERVER_STOP_DRY = "debug.server-stop.dry-run.success"
  }
}
