package com.daylifecraft.minigames.command.confirm

import com.daylifecraft.minigames.command.CommandsManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.condition.CommandCondition
import java.util.UUID

class ConfirmCommand : Command("confirm-action") {
  private val idArgument = ArgumentType.UUID("id")

  init {
    addSyntax(this::onConfirm, idArgument)

    condition =
      CommandCondition { sender: CommandSender, _ ->
        for (senderUuid in sendersUuid) {
          if (CommandsManager.getUuidFromSender(sender) == senderUuid) {
            return@CommandCondition true
          }
        }
        return@CommandCondition false
      }
  }

  private fun onConfirm(sender: CommandSender, context: CommandContext) {
    val id = context[idArgument]

    // Get confirm request
    val request = CommandsManager.getConfirmRequest(id)
    if (request != null) {
      // Confirm request if exists
      request.confirm(sender)

      // Delete request
      CommandsManager.deleteConfirmRequest(id)
    }
  }

  companion object {
    private val sendersUuid: MutableSet<UUID> = HashSet()

    /**
     * Add sender to confirm
     *
     * @param sender sender
     */
    fun addSender(sender: CommandSender) {
      sendersUuid.add(CommandsManager.getUuidFromSender(sender))
    }

    /**
     * Remove sender from confirm
     *
     * @param sender sender
     */
    fun removeSender(sender: CommandSender) {
      sendersUuid.remove(CommandsManager.getUuidFromSender(sender))
    }
  }
}
