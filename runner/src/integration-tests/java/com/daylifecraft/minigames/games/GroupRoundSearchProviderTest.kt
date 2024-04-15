package com.daylifecraft.minigames.games

import com.daylifecraft.minigames.Init.miniGamesSettingsManager
import com.daylifecraft.minigames.UtilsForTesting
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.queue.PlayerMiniGameQueueData
import com.daylifecraft.minigames.minigames.search.PlayerGroupRoundSearchProvider
import com.daylifecraft.minigames.minigames.settings.GeneralGameSettings
import io.mockk.clearMocks
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import net.minestom.server.entity.Player
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GroupRoundSearchProviderTest {
  @BeforeEach
  fun updateTestingInstance() {
    roundSearchProvider?.let { clearMocks(it) }
    roundSearchProvider = spyk(
      PlayerGroupRoundSearchProvider(
        fakePlayer1,
        listOf(fakePlayer2),
        generalGameSettings!!,
      ),
    )
  }

  @Test
  fun testDoesAllGroupPrepared() {
    mockkObject(PlayerMiniGameManager) {
      roundSearchProvider!!.onPlayerPrepared(
        fakePlayer1,
        PlayerMiniGameQueueData(generalGameSettings!!.name, roundSearchProvider!!),
      )
      roundSearchProvider!!.onPlayerPrepared(
        fakePlayer2,
        PlayerMiniGameQueueData(generalGameSettings!!.name, roundSearchProvider!!),
      )

      verify(exactly = 0) { PlayerMiniGameManager.removeLockedPlayer(fakePlayer1.uuid) }
      verify(exactly = 0) { PlayerMiniGameManager.removeLockedPlayer(fakePlayer2.uuid) }
    }
  }

  @Test
  fun testDoesAllGroupRejected() {
    mockkObject(PlayerMiniGameManager) {
      roundSearchProvider!!.onPlayerPrepared(
        fakePlayer1,
        PlayerMiniGameQueueData(generalGameSettings!!.name, roundSearchProvider!!),
      )
      roundSearchProvider!!.onPlayerCancelledPreparation(
        fakePlayer2,
        PlayerMiniGameQueueData(generalGameSettings!!.name, roundSearchProvider!!),
      )

      verify(exactly = 1) { PlayerMiniGameManager.removeLockedPlayer(fakePlayer1.uuid) }
      verify(exactly = 1) { PlayerMiniGameManager.removeLockedPlayer(fakePlayer2.uuid) }
    }
  }

  @Test
  fun testDoesGroupUnlockedWhenPlayerLeave() {
    mockkObject(PlayerMiniGameManager) {
      // Group successfully prepared
      roundSearchProvider!!.onPlayerPrepared(
        fakePlayer1,
        PlayerMiniGameQueueData(generalGameSettings!!.name, roundSearchProvider!!),
      )
      roundSearchProvider!!.onPlayerPrepared(
        fakePlayer2,
        PlayerMiniGameQueueData(generalGameSettings!!.name, roundSearchProvider!!),
      )

      // Some player leave from server
      roundSearchProvider!!.onPlayerRejectRoundSearch(fakePlayer2)
      verify { PlayerMiniGameManager.removeFromSearchQueueAndUnlockGroup(fakePlayer1.uuid) }
    }
  }

  companion object {
    private val fakePlayer1: Player = UtilsForTesting.initFakePlayer("GProviderT1")

    private val fakePlayer2: Player = UtilsForTesting.initFakePlayer("GProviderT2")

    private var roundSearchProvider: PlayerGroupRoundSearchProvider? = null

    private var generalGameSettings: GeneralGameSettings? = null

    @BeforeAll
    @Throws(InterruptedException::class)
    @JvmStatic
    fun setup() {
      UtilsForTesting.waitUntilPlayerJoin(fakePlayer1, fakePlayer2)

      generalGameSettings = miniGamesSettingsManager.getGeneralGameSettings(
        miniGamesSettingsManager.loadedMiniGamesIds.firstOrNull() ?: "",
      )
    }

    @AfterAll
    @JvmStatic
    fun kickPlayers() {
      fakePlayer1.kick("")
      fakePlayer2.kick("")
    }
  }
}
