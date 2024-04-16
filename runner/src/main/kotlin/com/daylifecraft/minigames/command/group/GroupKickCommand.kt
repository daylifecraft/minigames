package com.daylifecraft.minigames.command.group

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.argument.ArgumentGroupUsername
import com.daylifecraft.minigames.profile.group.PlayersGroup
import com.daylifecraft.minigames.profile.group.PlayersGroupManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.entity.Player
import java.util.UUID

class GroupKickCommand :
  AbstractGroupCommand("kick"),
  SubCommand {
  private val usernameArgument = ArgumentGroupUsername("username")

  init {
    isGroupRequired = true
    isLeaderOnly = true

    addSyntax(usernameArgument)
  }

  /**
   * This method will be called when the command is used
   *
   * @param sender who send command
   * @param context command context
   * @param senderGroup group
   * @param playerLanguage language
   */
  public override fun onCommandUse(
    sender: Player,
    context: CommandContext,
    senderGroup: PlayersGroup?,
    playerLanguage: PlayerLanguage,
  ) {
    // Getting target to kick
    val targetName = context.getOrDefault(usernameArgument, "")
    val targetUUID = PlayerManager.getPlayerUuid(targetName)

    kick(sender, targetName, targetUUID, senderGroup!!, playerLanguage)
  }

  companion object {
    /**
     * Kick player from group
     *
     * @param executor who send command
     * @param targetName player name to kick
     * @param targetUUID player uuid to kick
     * @param playersGroup group to kick from
     * @param playerLanguage language
     */
    fun kick(
      executor: Player,
      targetName: String,
      targetUUID: UUID,
      playersGroup: PlayersGroup,
      playerLanguage: PlayerLanguage,
    ) {
      // Check target group
      if (!playersGroup.isContainsPlayer(targetUUID)) {
        playerLanguage.sendMiniMessage("group.target-not-in-group", "targetPlayer" to targetName)
        return
      }

      if (targetUUID == executor.uuid) {
        playerLanguage.sendMiniMessage("group.kick.self-fail")
        return
      }

      // Remove target from own group and send group message
      playersGroup.removePlayer(targetUUID)
      PlayersGroupManager.sendMessageToPlayerGroup(
        playersGroup,
        "group.kick.success",
        "player" to targetName,
      )
      // If target is online send message
      val targetPlayer =
        MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(targetUUID)
      if (targetPlayer != null) {
        PlayerLanguage.get(targetPlayer).sendMiniMessage("group.kicked")
      }

      // Disband a single-player group
      if (playersGroup.groupSize <= 1) {
        PlayersGroupManager.removeGroup(playersGroup)
      }
    }
  }
}
