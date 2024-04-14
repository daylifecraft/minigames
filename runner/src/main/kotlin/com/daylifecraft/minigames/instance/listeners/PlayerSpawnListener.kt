package com.daylifecraft.minigames.instance.listeners

import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.logging.foundation.LogEvent
import com.daylifecraft.common.util.NetworkUtil.getPlayerAddress
import com.daylifecraft.common.util.NetworkUtil.getPlayerIp
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.instance.CraftInstancesManager
import com.daylifecraft.minigames.listener.Listener
import com.daylifecraft.minigames.util.sendStackTrace
import net.minestom.server.event.player.PlayerSpawnEvent

class PlayerSpawnListener :
  Listener<PlayerSpawnEvent>(
    PlayerSpawnEvent::class.java,
  ) {
  override fun onCalled(event: PlayerSpawnEvent) {
    val player = event.player
    val craftInstancesManager = CraftInstancesManager.get()
    val spawnInstance = event.spawnInstance
    val lastInstance = craftInstancesManager.getLastPlayerInstance(player)

    val leaveCraftInstance =
      craftInstancesManager.getInstance { craftInstance ->
        craftInstance.instance == lastInstance
      }

    val spawnCraftInstance =
      craftInstancesManager.getInstance { craftInstance ->
        craftInstance.instance == spawnInstance
      }

    if (leaveCraftInstance != null) {
      craftInstancesManager.notifyPlayerLeave(lastInstance!!, player)
    }

    PlayerManager.resetPlayer(player)

    // Check if the instance belongs to our engine or is a core instance. We call the logic only if
    // the instance is from our engine
    if (spawnCraftInstance != null) {
      sendStackTrace(player, "targetInstance=" + spawnCraftInstance.instanceUniqueId)
      spawnCraftInstance.setupPlayer(player).applyToPlayer(player)
      craftInstancesManager.notifyPlayerSpawn(spawnInstance, player)
    }

    val remoteAddress = getPlayerAddress(player)
    val ip = getPlayerIp(remoteAddress)
    val port = remoteAddress.port.toLong()
    val uuid = player.uuid

    LOGGER.build(LogEvent.PLAYER_JOINED) {
      message("Player connected to server")
      player(player)
      details("playerUsername", player.username)
      details("playerIp", ip)
      details("playerPort", port)
      details("uuid", uuid)
    }
  }

  companion object {
    private val LOGGER = createLogger<PlayerSpawnListener>()
  }
}
