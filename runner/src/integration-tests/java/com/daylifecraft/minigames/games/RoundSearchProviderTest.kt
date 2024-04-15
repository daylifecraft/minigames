package com.daylifecraft.minigames.games

import com.daylifecraft.minigames.UtilsForTesting
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.search.IRoundSearchProvider
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import net.minestom.server.entity.Player
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class RoundSearchProviderTest {
  @Test
  fun testDoesPlayerCannotBePreparedWhenLocked() {
    mockkObject(PlayerMiniGameManager) {
      every { PlayerMiniGameManager.isPlayerLocked(fakePlayer.uuid) } returns true

      assertFalse(
        roundSearchProvider.canBePrepared(fakePlayer),
        message = "Expected that locked player cannot be prepared",
      )
    }
  }

  @Test
  fun testDoesPlayerCanBePrepared() {
    mockkObject(PlayerMiniGameManager) {
      every { PlayerMiniGameManager.isPlayerLocked(fakePlayer.uuid) } returns false

      assertTrue(
        roundSearchProvider.canBePrepared(fakePlayer),
        message = "Expected that does not locked player can be prepared",
      )
    }
  }

  companion object {
    private val roundSearchProvider = spyk<IRoundSearchProvider>()

    private val fakePlayer: Player = UtilsForTesting.initFakePlayer("GProviderT")

    @BeforeAll
    @Throws(InterruptedException::class)
    @JvmStatic
    fun setup() {
      UtilsForTesting.waitUntilPlayerJoin(fakePlayer)
    }

    @AfterAll
    @JvmStatic
    fun kickPlayer() {
      fakePlayer.kick("")
    }
  }
}
