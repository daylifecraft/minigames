package com.daylifecraft.minigames.command.debug.towerdefence

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import com.daylifecraft.minigames.command.debug.towerdefence.subcommands.DebugModeSwitchTowerCommand
import com.daylifecraft.minigames.command.debug.towerdefence.subcommands.GetCurrencyTowerCommand
import com.daylifecraft.minigames.command.debug.towerdefence.subcommands.PlaceTowerCommand
import com.daylifecraft.minigames.command.debug.towerdefence.subcommands.RemoveTowerCommand
import com.daylifecraft.minigames.command.debug.towerdefence.subcommands.SetCurrencyTowerCommand
import com.daylifecraft.minigames.command.debug.towerdefence.subcommands.SetWaveTowerCommand
import com.daylifecraft.minigames.command.debug.towerdefence.subcommands.SpawnMonsterTowerCommand
import com.daylifecraft.minigames.command.debug.towerdefence.subcommands.StartWavesUpdateTowerCommand
import com.daylifecraft.minigames.command.debug.towerdefence.subcommands.StopWavesUpdateTowerCommand
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.condition.CommandCondition
import net.minestom.server.entity.Player

class DebugTowerDefenceCommand :
  AbstractDebugCommand("td"),
  SubCommand {

  init {
    condition = CommandCondition { sender, _ ->
      val roundInstance = PlayerMiniGameManager.getMiniGameRoundByCondition { instance ->
        instance!!.getRoundPlayerSettings().containsKey(sender) || instance.getSpectatorsList().contains(sender)
      }

      return@CommandCondition sender is Player && roundInstance != null && roundInstance is TowerDefenceInstance
    }

    addSubcommand(PlaceTowerCommand())
    addSubcommand(GetCurrencyTowerCommand())
    addSubcommand(SetCurrencyTowerCommand())
    addSubcommand(StopWavesUpdateTowerCommand())
    addSubcommand(StartWavesUpdateTowerCommand())
    addSubcommand(SetWaveTowerCommand())
    addSubcommand(RemoveTowerCommand())
    addSubcommand(SpawnMonsterTowerCommand())
    addSubcommand(DebugModeSwitchTowerCommand())
  }

  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
  }
}
