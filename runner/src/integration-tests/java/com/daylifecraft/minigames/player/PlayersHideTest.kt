package com.daylifecraft.minigames.player;

import com.daylifecraft.minigames.PlayerManager;
import com.daylifecraft.minigames.UtilsForTesting;
import com.daylifecraft.minigames.database.DatabaseManager;
import com.daylifecraft.minigames.instance.instances.lobby.LobbyInstance;
import com.daylifecraft.minigames.profile.player.PlayerProfile;
import com.daylifecraft.minigames.profile.settings.SettingsProfile;
import net.minestom.server.entity.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlayersHideTest {

  private static Player fakePlayer;

  private static final LobbyInstance lobbyInstance = new LobbyInstance();

  private static final SettingsProfile playerSettings = Mockito.mock(SettingsProfile.class);

  private static final PlayerProfile playerProfile = Mockito.mock(PlayerProfile.class);

  @BeforeAll
  static void start() throws InterruptedException {
    fakePlayer = UtilsForTesting.initFakePlayer("HideTest1");

    UtilsForTesting.waitUntilPlayerJoin(fakePlayer);

    Mockito.doReturn(playerSettings).when(playerProfile).getSettings();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testPlayerViewLoaded(boolean hidePlayers) {
    try (MockedStatic<DatabaseManager> databaseManager = Mockito.mockStatic(DatabaseManager.class);
         MockedStatic<PlayerManager> playerManager = Mockito.mockStatic(PlayerManager.class)) {
      Mockito.doReturn(hidePlayers).when(playerSettings).getHidePlayers();
      databaseManager
        .when(() -> DatabaseManager.getPlayerProfile(ArgumentMatchers.eq(fakePlayer)))
        .thenReturn(playerProfile);

      lobbyInstance.playerJoin(fakePlayer);

      playerManager.verify(() -> PlayerManager.setPlayerHide(fakePlayer, hidePlayers));
    }
  }

  @Test
  void testPlayerViewWhenLeaveInstance() {
    PlayerManager.setPlayerHide(fakePlayer, true);

    try (MockedStatic<PlayerManager> playerManager = Mockito.mockStatic(PlayerManager.class)) {
      lobbyInstance.playerLeave(fakePlayer);

      playerManager.verify(() -> PlayerManager.setPlayerHide(fakePlayer, false));
    }
  }

  @AfterAll
  public static void kickPlayer() {
    fakePlayer.kick("");
  }
}
