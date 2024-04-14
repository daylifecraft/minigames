package com.daylifecraft.minigames.command.punishment

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.common.util.extensions.minestom.sendMiniMessage
import com.daylifecraft.common.util.extensions.miniMessage
import com.daylifecraft.minigames.PermissionManager
import com.daylifecraft.minigames.command.AbstractPermissionCommand
import com.daylifecraft.minigames.command.CommandsManager.getSenderLanguage
import com.daylifecraft.minigames.command.PlayerFindParameter
import com.daylifecraft.minigames.profile.punishment.PunishmentProfile
import com.daylifecraft.minigames.profile.punishment.PunishmentsManager
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

private const val PAGE_SIZE = 5
private const val SPACING = 18

class PunishmentListCommand :
  AbstractPermissionCommand("list"),
  SubCommand {
  override val permission = "punishments.list"

  private val playerArgument = ArgumentType.String("player")
  private val pageArgument = ArgumentType.Integer("page")

  init {
    addSyntax(this::onExecute, playerArgument)
    addSyntax(this::onExecute, playerArgument, pageArgument)
  }

  private fun onExecute(sender: CommandSender, context: CommandContext) {
    val language = getSenderLanguage(sender)

    var page = (context[pageArgument] ?: 1) - 1

    page = max(0, page)

    val playerString = context[playerArgument]

    // Get find parameter from sender`s input
    val findParameter = PlayerFindParameter.get(playerString)

    val profiles =
      when (findParameter) {
        PlayerFindParameter.USERNAME -> {
          PunishmentsManager.getList(playerString)
        }

        PlayerFindParameter.UUID -> {
          PunishmentsManager.getList(UUID.fromString(playerString))
        }

        else -> {
          getSenderLanguage(sender).sendMiniMessage("punishments.fail.wrong-args")
          return
        }
      }

    if (profiles.isEmpty()) {
      language.sendMiniMessage(
        "commands.punishments.list.fail.empty",
        "targetPlayer" to playerString,
      )
      return
    }

    page = min((profiles.size - 1) / PAGE_SIZE, page)

    // Show list of punishments to player
    showList(sender, profiles.reversed(), playerString, page)
  }

  companion object {
    private fun showList(
      sender: CommandSender,
      profiles: List<PunishmentProfile>,
      username: String,
      page: Int,
    ) {
      // Get sender`s language
      val language = getSenderLanguage(sender)

      // TODO i18n it
      val startMessage =
        "\n\n\n\n\n\n\n\n\n\n\n${
          language.string(
            "punishments.list-for-username",
            "targetPlayer" to username,
          )
        }\n------------------------------\n${language.string("punishments.list-string-1")}\n"

      val endMessage =
        language.string(
          "punishments.list-pages",
          "punishmentsListTargetUser" to username,
          "punishmentsListPreviousPage" to "" + (page),
          "punishmentsListCurrentPage" to "" + (page + 1),
          "punishmentsListNextPage" to "" + (page + 2),
        )

      // Send start message to sender
      sender.sendMessage(startMessage.miniMessage())

      // Get profiles count in page
      val pageStartIndex = min(page * PAGE_SIZE, profiles.size)
      val pageEndIndex = min(pageStartIndex + PAGE_SIZE, profiles.size)

      if (profiles.isNotEmpty()) {
        profiles[0].setLessDetails()
        val firstDetails = profiles[0].getDetails(language)

        val firstDetailsText = MiniMessage.miniMessage().serialize(firstDetails.show())

        var spaces: Long = 0
        val spaceLength = MiniMessage.miniMessage().stripTags(firstDetailsText).length

        for (i in pageStartIndex until pageEndIndex) {
          // Set less information to show in player details
          profiles[i].setLessDetails()

          // Check weather sender has permission
          if (sender is Player &&
            PermissionManager.hasPermission(
              sender,
              "punishments.list",
              profiles[i].type,
            )
          ) {
            // Send message to sender with punishment details
            val showMessage =
              profiles[i].getDetails(getSenderLanguage(sender)).show()

            sender.sendMessage(showMessage)
          }

          spaces++
        }

        for (i in 0 until PAGE_SIZE - spaces) {
          sender.sendMessage(" ".repeat(spaceLength + SPACING))
        }
      }

      // Send end message to sender
      sender.sendMiniMessage(endMessage)
    }
  }
}
