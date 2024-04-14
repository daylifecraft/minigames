package com.daylifecraft.minigames.games;

import com.daylifecraft.minigames.event.player.minigame.PlayerPreparationEndEvent;
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager;
import com.daylifecraft.minigames.minigames.queue.PlayerMiniGameQueueData;
import com.daylifecraft.minigames.minigames.search.IRoundSearchProvider;

import java.util.UUID;

import net.minestom.server.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class MiniGamesManagerTest {

  private static final Player fakePlayer = Mockito.mock(Player.class);

  private static final UUID ZERO_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

  private static final IRoundSearchProvider roundSearchProvider =
    Mockito.spy(IRoundSearchProvider.class);

  private static final String MINI_GAME_ID = "awesomeMiniGame";

  @BeforeAll
  static void setup() {
    Mockito.doReturn(ZERO_UUID).when(fakePlayer).getUuid();
    Mockito.doReturn(Mockito.mock(Player.PlayerSettings.class)).when(fakePlayer).getSettings();
    Mockito.doReturn("Bot").when(fakePlayer).getUsername();
  }

  @Test
  void testDoesPlayerLockedOnPreparation() {
    try (MockedStatic<PlayerMiniGameManager> miniGameManager =
           Mockito.mockStatic(PlayerMiniGameManager.class)) {
      miniGameManager
        .when(
          () ->
            PlayerMiniGameManager.preparePlayerForRoundSearch(
              fakePlayer, MINI_GAME_ID, roundSearchProvider))
        .thenCallRealMethod();
      miniGameManager
        .when(
          () ->
            PlayerMiniGameManager.preparePlayerForRoundSearch(
              ArgumentMatchers.eq(fakePlayer),
              ArgumentMatchers.eq(MINI_GAME_ID),
              ArgumentMatchers.eq(roundSearchProvider),
              ArgumentMatchers.any(),
              ArgumentMatchers.any()))
        .thenCallRealMethod();

      PlayerMiniGameManager.preparePlayerForRoundSearch(
        fakePlayer, MINI_GAME_ID, roundSearchProvider);

      miniGameManager.verify(() -> PlayerMiniGameManager.addLockedPlayer(ZERO_UUID, MINI_GAME_ID));
    }
  }

  @Test
  void testDoesPlayerDoesNotPreparedWhenInGame() {
    try (MockedStatic<PlayerMiniGameManager> miniGameManager =
           Mockito.mockStatic(PlayerMiniGameManager.class)) {
      miniGameManager
        .when(
          () ->
            PlayerMiniGameManager.preparePlayerForRoundSearch(
              fakePlayer, MINI_GAME_ID, roundSearchProvider))
        .thenCallRealMethod();
      miniGameManager
        .when(
          () ->
            PlayerMiniGameManager.preparePlayerForRoundSearch(
              ArgumentMatchers.eq(fakePlayer),
              ArgumentMatchers.eq(MINI_GAME_ID),
              ArgumentMatchers.eq(roundSearchProvider),
              ArgumentMatchers.any(),
              ArgumentMatchers.any()))
        .thenCallRealMethod();

      miniGameManager.when(() -> PlayerMiniGameManager.isPlayerLocked(ZERO_UUID)).thenReturn(true);

      PlayerMiniGameManager.preparePlayerForRoundSearch(
        fakePlayer, MINI_GAME_ID, roundSearchProvider);

      miniGameManager.verify(
        () -> PlayerMiniGameManager.addLockedPlayer(ZERO_UUID, MINI_GAME_ID), Mockito.never());
    }
  }

  @Test
  void testDoesPlayerUnlockedWhenCancelledPreparation() {
    PlayerMiniGameQueueData queueData =
      new PlayerMiniGameQueueData(MINI_GAME_ID, roundSearchProvider);

    try (MockedStatic<PlayerMiniGameManager> miniGameManager =
           Mockito.mockStatic(PlayerMiniGameManager.class)) {
      miniGameManager
        .when(
          () ->
            PlayerMiniGameManager.onPlayerPreparationEnd(
              fakePlayer, queueData, PlayerPreparationEndEvent.PreparationResult.CANCELLED))
        .thenCallRealMethod();

      PlayerMiniGameManager.onPlayerPreparationEnd(
        fakePlayer, queueData, PlayerPreparationEndEvent.PreparationResult.CANCELLED);

      miniGameManager.verify(() -> PlayerMiniGameManager.removeLockedPlayer(ZERO_UUID));
    }
  }

  @Test
  void testDoesPlayerProcessedWhenPrepared() {
    PlayerMiniGameQueueData queueData =
      new PlayerMiniGameQueueData(MINI_GAME_ID, roundSearchProvider);

    try (MockedStatic<PlayerMiniGameManager> miniGameManager =
           Mockito.mockStatic(PlayerMiniGameManager.class)) {
      miniGameManager
        .when(
          () ->
            PlayerMiniGameManager.onPlayerPreparationEnd(
              fakePlayer,
              queueData,
              PlayerPreparationEndEvent.PreparationResult.ACTIVE_SEARCH))
        .thenCallRealMethod();

      PlayerMiniGameManager.onPlayerPreparationEnd(
        fakePlayer, queueData, PlayerPreparationEndEvent.PreparationResult.ACTIVE_SEARCH);

      miniGameManager.verify(
        () -> PlayerMiniGameManager.removeLockedPlayer(ZERO_UUID), Mockito.never());
    }
  }
}
