package com.daylifecraft.minigames.player

import com.daylifecraft.minigames.UtilsForTesting
import com.daylifecraft.minigames.instance.CraftInstancesManager
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.player.PlayerDisconnectEvent
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class PlayerLastInstanceTest {
  @Test
  @Order(1)
  fun testPlayerLastInstance() {
    val lastPlayerInstance = CraftInstancesManager.get().getLastPlayerInstance(fakePlayer1)

    assertNotNull(lastPlayerInstance, "The player should have last instance")
  }

  @Test
  @Order(2)
  fun testPlayerDisconnectInstance() {
    fakePlayer1.playerConnection.disconnect()
    EventDispatcher.call(PlayerDisconnectEvent(fakePlayer1))
    val lastPlayerInstance = CraftInstancesManager.get().getLastPlayerInstance(fakePlayer1)

    assertNull(lastPlayerInstance, "Last instance of the player must be deleted")
  }

  companion object {
    private lateinit var fakePlayer1: Player

    @BeforeAll
    @Throws(InterruptedException::class)
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
