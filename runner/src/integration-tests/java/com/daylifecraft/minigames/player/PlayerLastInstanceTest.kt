package com.daylifecraft.minigames.player

import com.daylifecraft.minigames.UtilsForTesting
import com.daylifecraft.minigames.instance.CraftInstancesManager
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.player.PlayerDisconnectEvent
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class PlayerLastInstanceTest {

  @Test
  fun testPlayerLastInstance() {
    val lastPlayerInstance = CraftInstancesManager.get().getLastPlayerInstance(fakePlayer1)

    assertNotNull(lastPlayerInstance, "The player should have last instance")
  }

  @Test
  fun testPlayerDisconnectInstance() {
    fakePlayer1.playerConnection.disconnect()
    EventDispatcher.call(PlayerDisconnectEvent(fakePlayer1))
    val lastPlayerInstance = CraftInstancesManager.get().getLastPlayerInstance(fakePlayer1)

    assertNull(lastPlayerInstance, "Last instance of the player must be deleted")
  }

  companion object {
    private lateinit var fakePlayer1: Player

    @BeforeAll
    @JvmStatic
    fun start() {
      fakePlayer1 = UtilsForTesting.initFakePlayer("LastInstance")

      UtilsForTesting.waitUntilPlayerJoin(fakePlayer1)
    }

    @AfterAll
    @JvmStatic
    fun kickPlayers() {
      fakePlayer1.kick("")
    }
  }
}
