package com.daylifecraft.minigames.command.rounds

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.command.AbstractPermissionCommand
import com.daylifecraft.minigames.command.CommandsManager.parseUuidOrUserNameToUuid
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import org.bson.types.ObjectId

/**
 * If the round has already been initialized, but it has not been completed or canceled, it allows
 * you to go to the specified round in observation mode, accepts the round ID or the nickname/UUID
 * of the player as a mandatory argument
 */
class RoundsSpectateCommand internal constructor() :
  AbstractPermissionCommand("spectate"),
  SubCommand {
  override val permission = "rounds.spectate"

  private val playerOrIdArgument = ArgumentType.String("playerOrId")

  init {
    val condition = condition
    setCondition { sender: CommandSender, commandString: String? ->
      if (condition == null) {
        return@setCondition false
      }
      val notInRound = !PlayerMiniGameManager.isPlayerLocked((sender as Player).uuid)
      condition.canUse(sender, commandString) && notInRound
    }

    addConditionalSyntax(getCondition(), this::onExecute)
    addConditionalSyntax(getCondition(), this::onExecute, playerOrIdArgument)
  }

  private fun onExecute(sender: CommandSender, context: CommandContext) {
    val senderPlayer = sender as Player
    val playerLanguage = PlayerLanguage.get(senderPlayer)

    if (!context.has(playerOrIdArgument)) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.required-args-not-found")
      return
    }

    val playerOrId = context[playerOrIdArgument]

    if (ObjectId.isValid(playerOrId)) {
      val roundId = ObjectId(playerOrId)

      val roundInstance = PlayerMiniGameManager.getMiniGameRoundByObjectId(roundId)

      if (roundInstance == null) {
        playerLanguage.sendMiniMessage("commands.rounds.spectate.fail.round-not-found")
        return
      }

      roundInstance.addSpectator(senderPlayer, null)
      playerLanguage.sendMiniMessage(
        "commands.rounds.spectate.success",
        "roundId" to roundId.toString(),
      )
      senderPlayer.refreshCommands()
      return
    }

    val targetUuid = parseUuidOrUserNameToUuid(playerOrId)
    if (targetUuid == null) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.wrong-args")
      return
    }

    val target = PlayerManager.getPlayerByUuid(targetUuid)

    if (target == null) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.non-existent-player")
      return
    }

    val roundInstance =
      PlayerMiniGameManager.getMiniGameRoundByMember(target)

    if (roundInstance == null) {
      playerLanguage.sendMiniMessage("commands.rounds.spectate.fail.round-not-found")
      return
    }

    roundInstance.addSpectator(senderPlayer, target)
    playerLanguage.sendMiniMessage(
      "commands.rounds.spectate.success",
      "roundId" to roundInstance.roundProfile.id.toString(),
    )
    senderPlayer.refreshCommands()
  }
}
