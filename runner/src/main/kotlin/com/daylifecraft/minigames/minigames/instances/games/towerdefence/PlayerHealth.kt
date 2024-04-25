package com.daylifecraft.minigames.minigames.instances.games.towerdefence

import java.util.concurrent.atomic.AtomicInteger

class PlayerHealth(
  startPlayerHealth: Int,
) {

  private val playerHealth: AtomicInteger = AtomicInteger(startPlayerHealth)

  fun doDamage(damage: Int): Boolean {
    playerHealth.set(playerHealth.get() - damage)

    return playerHealth.get() <= 0
  }

  fun getHealth(): Int = playerHealth.get()
}
