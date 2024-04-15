package com.daylifecraft.minigames.player

import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.fakeplayer.FakePlayer
import com.daylifecraft.minigames.instance.instances.lobby.LobbyInstance
import com.daylifecraft.minigames.profile.player.PlayerProfile
import com.daylifecraft.minigames.profile.settings.SettingsProfile
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class PlayersHideTest {

  private val lobbyInstance = LobbyInstance()

  @RelaxedMockK
  private lateinit var fakePlayer: FakePlayer

  @RelaxedMockK
  private lateinit var playerSettings: SettingsProfile

  @RelaxedMockK
  private lateinit var playerProfile: PlayerProfile

  @BeforeEach
  fun setup() {
    every { playerProfile.settings } returns playerSettings
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun testPlayerViewLoaded(hidePlayers: Boolean) {
    mockkObject(DatabaseManager, PlayerManager) {
      every { playerSettings.hidePlayers } returns hidePlayers
      every { DatabaseManager.getPlayerProfile(fakePlayer) } returns playerProfile

      lobbyInstance.playerJoin(fakePlayer)
      verify(exactly = 1) { PlayerManager.setPlayerHide(fakePlayer, hidePlayers) }
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
}
