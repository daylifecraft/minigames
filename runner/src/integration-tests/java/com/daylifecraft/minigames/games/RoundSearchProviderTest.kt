package com.daylifecraft.minigames.games;

import com.daylifecraft.minigames.UtilsForTesting;
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager;
import com.daylifecraft.minigames.minigames.search.IRoundSearchProvider;
import net.minestom.server.entity.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class RoundSearchProviderTest {

  private static final IRoundSearchProvider roundSearchProvider =
    Mockito.spy(IRoundSearchProvider.class);

  private static final Player fakePlayer = UtilsForTesting.initFakePlayer("GProviderT");

  @BeforeAll
  static void setup() throws InterruptedException {
    UtilsForTesting.waitUntilPlayerJoin(fakePlayer);
  }

  @Test
  void testDoesPlayerCannotBePreparedWhenLocked() {
    try (MockedStatic<PlayerMiniGameManager> playerMiniGameManager =
           Mockito.mockStatic(PlayerMiniGameManager.class)) {
      playerMiniGameManager
        .when(() -> PlayerMiniGameManager.isPlayerLocked(fakePlayer.getUuid()))
        .thenReturn(true);

      Assertions.assertFalse(
        roundSearchProvider.canBePrepared(fakePlayer),
        "Expected that locked player cannot be prepared");
    }
  }

  @Test
  void testDoesPlayerCanBePrepared() {
    try (MockedStatic<PlayerMiniGameManager> playerMiniGameManager =
           Mockito.mockStatic(PlayerMiniGameManager.class)) {
      playerMiniGameManager
        .when(() -> PlayerMiniGameManager.isPlayerLocked(fakePlayer.getUuid()))
        .thenReturn(false);

      Assertions.assertTrue(
        roundSearchProvider.canBePrepared(fakePlayer),
        "Expected that does not locked player can be prepared");
    }
  }

  @AfterAll
  static void kickPlayer() {
    fakePlayer.kick("");
  }
}
