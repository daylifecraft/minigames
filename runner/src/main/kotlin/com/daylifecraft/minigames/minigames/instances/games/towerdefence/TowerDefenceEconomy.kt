package com.daylifecraft.minigames.minigames.instances.games.towerdefence

import net.minestom.server.entity.Player
import java.util.UUID

class TowerDefenceEconomy : IEconomyProvider {

  private val usersBalanceStorage: MutableMap<UUID, Long> = mutableMapOf()

  override fun getBalance(player: Player): Long = usersBalanceStorage.getOrDefault(player.uuid, 0)

  override fun addBalance(player: Player, value: Number) {
    val playerUuid = player.uuid
    val playerBalance = getBalance(player)

    require(playerBalance + value.toLong() >= 0) {
      "Player ${player.username} does not have enough moneys for operation: $playerBalance add $value"
    }

    usersBalanceStorage[playerUuid] = getBalance(player) + value.toLong()
  }

  override fun setBalance(player: Player, value: Number) {
    require(value.toLong() >= 0) {
      "Tried to setup negative player balance ${player.username} $value"
    }

    usersBalanceStorage[player.uuid] = value.toLong()
  }

  override fun hasOnBalance(player: Player, value: Number): Boolean = getBalance(player) >= value.toLong()
}
