package com.daylifecraft.minigames.command.group

import com.daylifecraft.minigames.command.CommandsManager.getUuidFromSender
import com.daylifecraft.minigames.profile.group.PlayersGroup
import com.daylifecraft.minigames.profile.group.PlayersGroupManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.condition.CommandCondition
import net.minestom.server.entity.Player

/** Abstract class to generalize group checking  */
abstract class AbstractGroupCommand protected constructor(
  command: String,
  vararg aliases: String,
) : Command(command, *aliases) {
  protected var isGroupRequired = false

  protected var isLeaderOnly = false

  init {
    condition = CommandCondition { sender: CommandSender?, _ -> (sender is Player) }
  }

  fun addSyntax(vararg arguments: Argument<*>) {
    addSyntax(this::onExecute, *arguments)
  }

  /**
   * This method will be called when the command is used
   *
   * @param sender who send command
   * @param context command context
   */
  protected fun onExecute(sender: CommandSender, context: CommandContext) {
    // Getting Sender
    val senderUUID = getUuidFromSender(sender)
    val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(senderUUID) ?: return

    val playerLanguage = PlayerLanguage.get(player)

    // Check whether the player is in the group or not
    val groupByPlayer: PlayersGroup? = PlayersGroupManager.getGroupByPlayer(player)
    if (isGroupRequired && (groupByPlayer == null)) {
      playerLanguage.sendMiniMessage("group.not-in-group-fail")
      return
    }

    // Check whether the player is in the group and is leader
    if (isLeaderOnly &&
      (groupByPlayer == null || groupByPlayer.playerLeaderUUID != senderUUID)
    ) {
      playerLanguage.sendMiniMessage("group.not-leader-fail")
      return
    }

    onCommandUse(player, context, groupByPlayer, playerLanguage)
  }

  /**
   * Shell for Group commands, that check conditions at first and getting player group and language
   * for ease
   *
   * @param sender Player sender
   * @param context CommandContext
   * @param senderGroup Sender group or null
   * @param playerLanguage Sender language
   */
  protected abstract fun onCommandUse(
    sender: Player,
    context: CommandContext,
    senderGroup: PlayersGroup?,
    playerLanguage: PlayerLanguage,
  )
}
