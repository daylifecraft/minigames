package com.daylifecraft.minigames.command.group

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.argument.ArgumentGroupUsername
import com.daylifecraft.minigames.profile.group.PlayersGroup
import com.daylifecraft.minigames.profile.group.PlayersGroupManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.entity.Player

class GroupNewLeaderCommand :
  AbstractGroupCommand("new-leader"),
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
    // Wrap username to uuid
    val userName = context.getOrDefault(usernameArgument, "")
    val playerUUID = PlayerManager.getPlayerUuid(userName)

    // Check if target in that group
    if (!senderGroup!!.isContainsPlayer(playerUUID)) {
      playerLanguage.sendMiniMessage("group.target-not-in-group", "targetPlayer" to userName)
      return
    }

    // Cannot pass leader to yourself.
    if (playerUUID == senderGroup.playerLeaderUUID) {
      playerLanguage.sendMiniMessage("group.new-leader.self-fail")
      return
    }

    senderGroup.playerLeaderUUID = playerUUID
    PlayersGroupManager.sendMessageToPlayerGroup(
      senderGroup,
      "group.new-leader",
      "player" to userName,
    )
  }
}
