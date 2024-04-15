package com.daylifecraft.minigames.games;

import com.daylifecraft.minigames.Init;
import com.daylifecraft.minigames.UtilsForTesting;
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager;
import com.daylifecraft.minigames.minigames.queue.PlayerMiniGameQueueData;
import com.daylifecraft.minigames.minigames.search.PlayerGroupRoundSearchProvider;
import com.daylifecraft.minigames.minigames.settings.GeneralGameSettings;

import java.util.Collections;

import net.minestom.server.entity.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class GroupRoundSearchProviderTest {

  private static final Player fakePlayer1 = UtilsForTesting.initFakePlayer("GProviderT1");

  private static final Player fakePlayer2 = UtilsForTesting.initFakePlayer("GProviderT2");

  private static PlayerGroupRoundSearchProvider roundSearchProvider;

  private static GeneralGameSettings generalGameSettings;

  @BeforeAll
  static void setup() throws InterruptedException {
    UtilsForTesting.waitUntilPlayerJoin(fakePlayer1, fakePlayer2);

    generalGameSettings =
      Init.getMiniGamesSettingsManager()
        .getGeneralGameSettings(
          Init.getMiniGamesSettingsManager().getLoadedMiniGamesIds().stream()
            .findAny()
            .orElse(""));
  }

  @BeforeEach
  void updateTestingInstance() {
    roundSearchProvider =
      Mockito.spy(
        new PlayerGroupRoundSearchProvider(
          fakePlayer1, Collections.singletonList(fakePlayer2), generalGameSettings));
  }

  @Test
  void testDoesAllGroupPrepared() {
    try (MockedStatic<PlayerMiniGameManager> playerMiniGameManager =
           Mockito.mockStatic(PlayerMiniGameManager.class)) {
      roundSearchProvider.onPlayerPrepared(
        fakePlayer1,
        new PlayerMiniGameQueueData(generalGameSettings.name(), roundSearchProvider));
      roundSearchProvider.onPlayerPrepared(
        fakePlayer2,
        new PlayerMiniGameQueueData(generalGameSettings.name(), roundSearchProvider));

      playerMiniGameManager.verify(
        () -> PlayerMiniGameManager.removeLockedPlayer(fakePlayer1.getUuid()), Mockito.never());
      playerMiniGameManager.verify(
        () -> PlayerMiniGameManager.removeLockedPlayer(fakePlayer2.getUuid()), Mockito.never());
    }
  }

  @Test
  void testDoesAllGroupRejected() {
    try (MockedStatic<PlayerMiniGameManager> playerMiniGameManager =
           Mockito.mockStatic(PlayerMiniGameManager.class)) {
      roundSearchProvider.onPlayerPrepared(
        fakePlayer1,
        new PlayerMiniGameQueueData(generalGameSettings.name(), roundSearchProvider));
      roundSearchProvider.onPlayerCancelledPreparation(
        fakePlayer2,
        new PlayerMiniGameQueueData(generalGameSettings.name(), roundSearchProvider));

      playerMiniGameManager.verify(
        () -> PlayerMiniGameManager.removeLockedPlayer(fakePlayer1.getUuid()));
      playerMiniGameManager.verify(
        () -> PlayerMiniGameManager.removeLockedPlayer(fakePlayer2.getUuid()));
    }
  }

  @Test
  void testDoesGroupUnlockedWhenPlayerLeave() {
    try (MockedStatic<PlayerMiniGameManager> playerMiniGameManager =
           Mockito.mockStatic(PlayerMiniGameManager.class)) {
      // Group successfully prepared
      roundSearchProvider.onPlayerPrepared(
        fakePlayer1,
        new PlayerMiniGameQueueData(generalGameSettings.name(), roundSearchProvider));
      roundSearchProvider.onPlayerPrepared(
        fakePlayer2,
        new PlayerMiniGameQueueData(generalGameSettings.name(), roundSearchProvider));

      // Some player leave from server
      roundSearchProvider.onPlayerRejectRoundSearch(fakePlayer2);

      playerMiniGameManager.verify(
        () -> PlayerMiniGameManager.removeFromSearchQueueAndUnlockGroup(fakePlayer1.getUuid()));
    }
  }

  @AfterAll
  static void kickPlayers() {
    fakePlayer1.kick("");
    fakePlayer2.kick("");
  }
}
