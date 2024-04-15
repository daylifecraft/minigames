package com.daylifecraft.minigames.player

import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.UtilsForTesting
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.instance.instances.lobby.LobbyInstance
import com.daylifecraft.minigames.profile.player.PlayerProfile
import com.daylifecraft.minigames.profile.settings.SettingsProfile
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import net.minestom.server.entity.Player
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class PlayersHideTest {
  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun testPlayerViewLoaded(hidePlayers: Boolean) {
    mockkObject(DatabaseManager, PlayerManager) {
      every { playerSettings.hidePlayers } returns hidePlayers
      every { DatabaseManager.getPlayerProfile(fakePlayer) } returns playerProfile

      lobbyInstance.playerJoin(fakePlayer)
      verify(exactly = 1) { PlayerManager.setPlayerHide(eq(fakePlayer), eq(hidePlayers)) }
    }
  }

  @Test
  fun testPlayerViewWhenLeaveInstance() {
    PlayerManager.setPlayerHide(fakePlayer, true)

    mockkObject(PlayerManager) {
      lobbyInstance.playerLeave(fakePlayer)
      verify(exactly = 1) { PlayerManager.setPlayerHide(fakePlayer, false) }
    }
  }

  companion object {
    private lateinit var fakePlayer: Player

    private val lobbyInstance = LobbyInstance()

    private val playerSettings = mockk<SettingsProfile>(relaxed = true)

    private val playerProfile = mockk<PlayerProfile>(relaxed = true)

    @BeforeAll
    @Throws(InterruptedException::class)
    @JvmStatic
    fun start() {
      fakePlayer = UtilsForTesting.initFakePlayer("HideTest1")

      UtilsForTesting.waitUntilPlayerJoin(fakePlayer)

      every { playerProfile.settings } returns playerSettings
    }

    @AfterAll
    @JvmStatic
    fun kickPlayer() {
      fakePlayer.kick("")
    }
  }
}
