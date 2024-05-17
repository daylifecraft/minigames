package com.daylifecraft.minigames.command.debug.towerdefence.subcommands

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.condition.CommandCondition
import net.minestom.server.entity.Player

abstract class AbstractTowerDefenceCommand protected constructor(
  command: String,
  vararg aliases: String,
) : AbstractDebugCommand(command, *aliases),
  SubCommand {

  protected var isMiniGameInstanceRequired: Boolean = true

  init {
    condition = CommandCondition { sender, _ ->
      val roundInstance = PlayerMiniGameManager.getMiniGameRoundByCondition { instance ->
        instance!!.getRoundPlayerSettings().containsKey(sender) || instance.getSpectatorsList().contains(sender)
      }

      return@CommandCondition sender is Player && roundInstance != null && roundInstance is TowerDefenceInstance
    }
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
  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    // Getting Sender
    val senderUUID = CommandsManager.getUuidFromSender(sender)
    val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(senderUUID) ?: return

    val playerLanguage = PlayerLanguage.get(player)

    // Check whether the player is in the round or not
    val miniGameInstance = PlayerMiniGameManager.getMiniGameRoundByCondition { abstractMiniGameInstance ->
      abstractMiniGameInstance!!.getRoundPlayerSettings().containsKey(player) ||
        abstractMiniGameInstance.getSpectatorsList().contains(player)
    }
    if (isMiniGameInstanceRequired && (miniGameInstance == null)) {
      playerLanguage.sendMiniMessage("debug.td.placetower.fail.not-in-td-round")
      return
    }

    if (miniGameInstance !is TowerDefenceInstance) {
      playerLanguage.sendMiniMessage("debug.td.placetower.fail.not-in-td-round")
      return
    }

    onCommandUse(player, context, miniGameInstance, playerLanguage)
  }

  /**
   * Shell for Group commands, that check conditions at first and getting player round instance and language
   * for ease
   *
   * @param sender Player sender
   * @param context CommandContext
   * @param miniGameInstance Sender Round instance
   * @param playerLanguage Sender language
   */
  protected abstract fun onCommandUse(
    player: Player,
    context: CommandContext,
    towerDefenceInstance: TowerDefenceInstance,
    playerLanguage: PlayerLanguage,
  )
}
