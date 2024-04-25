package com.daylifecraft.minigames.minigames.instances.games.towerdefence

import net.minestom.server.entity.Player

interface IEconomyProvider {

  fun getBalance(player: Player): Long

  fun addBalance(player: Player, value: Number)

  fun setBalance(player: Player, value: Number)

  fun hasOnBalance(player: Player, value: Number): Boolean
}
