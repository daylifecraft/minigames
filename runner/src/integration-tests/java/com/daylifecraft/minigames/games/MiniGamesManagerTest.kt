package com.daylifecraft.minigames.games

import com.daylifecraft.minigames.event.player.minigame.PlayerPreparationEndEvent
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.queue.PlayerMiniGameQueueData
import com.daylifecraft.minigames.minigames.search.IRoundSearchProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import net.minestom.server.entity.Player
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.UUID

internal class MiniGamesManagerTest {
  @Test
  fun testDoesPlayerLockedOnPreparation() {
    mockkObject(PlayerMiniGameManager) {
      PlayerMiniGameManager.preparePlayerForRoundSearch(
        fakePlayer,
        MINI_GAME_ID,
        roundSearchProvider,
      )

      verify { PlayerMiniGameManager.addLockedPlayer(ZERO_UUID, MINI_GAME_ID) }
    }
  }

  @Test
  fun testDoesPlayerDoesNotPreparedWhenInGame() {
    mockkObject(PlayerMiniGameManager) {
      every { PlayerMiniGameManager.isPlayerLocked(ZERO_UUID) } returns true

      PlayerMiniGameManager.preparePlayerForRoundSearch(
        fakePlayer,
        MINI_GAME_ID,
        roundSearchProvider,
      )

      verify(inverse = true) {
        PlayerMiniGameManager.addLockedPlayer(ZERO_UUID, MINI_GAME_ID)
      }
    }
  }

  @Test
  fun testDoesPlayerUnlockedWhenCancelledPreparation() {
    val queueData = PlayerMiniGameQueueData(MINI_GAME_ID, roundSearchProvider)

    mockkObject(PlayerMiniGameManager) {
      PlayerMiniGameManager.onPlayerPreparationEnd(
        fakePlayer,
        queueData,
        PlayerPreparationEndEvent.PreparationResult.CANCELLED,
      )

      verify { PlayerMiniGameManager.removeLockedPlayer(ZERO_UUID) }
    }
  }

  @Test
  fun testDoesPlayerProcessedWhenPrepared() {
    val queueData = PlayerMiniGameQueueData(MINI_GAME_ID, roundSearchProvider)

    mockkObject(PlayerMiniGameManager) {
      PlayerMiniGameManager.onPlayerPreparationEnd(
        fakePlayer,
        queueData,
        PlayerPreparationEndEvent.PreparationResult.ACTIVE_SEARCH,
      )

      verify(inverse = true) {
        PlayerMiniGameManager.removeLockedPlayer(ZERO_UUID)
      }
    }
  }

  companion object {
    private val fakePlayer = mockk<Player>(relaxed = true)

    private val ZERO_UUID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

    private val roundSearchProvider = spyk<IRoundSearchProvider>()

    private const val MINI_GAME_ID = "awesomeMiniGame"

    @BeforeAll
    @JvmStatic
    fun setup() {
      every { fakePlayer.uuid } returns (ZERO_UUID)
      every { fakePlayer.settings } returns mockk(relaxed = true)
      every { fakePlayer.username } returns "MiniGameManagerTestBot"
    }
  }
}
