package com.daylifecraft.minigames.command.rounds

import com.daylifecraft.minigames.command.AbstractPermissionCommand

/** Base command for /rounds <subcommand> </subcommand> */
class RoundsCommand : AbstractPermissionCommand("rounds") {
  override val permission = "rounds"

  init {
    addSubcommand(RoundsViewDetailsCommand())
    addSubcommand(RoundsForceEndCommand())
    addSubcommand(RoundsViewCommand())
    addSubcommand(RoundsSpectateCommand())
    addSubcommand(RoundsSpectateExitCommand())
    addSubcommand(RoundsSayCommand())
  }
}
