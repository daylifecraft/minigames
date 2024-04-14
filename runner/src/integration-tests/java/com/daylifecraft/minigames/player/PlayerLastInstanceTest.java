package com.daylifecraft.minigames.player;

import com.daylifecraft.minigames.UtilsForTesting;
import com.daylifecraft.minigames.instance.CraftInstancesManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.Instance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlayerLastInstanceTest {

  private static Player fakePlayer1;

  @BeforeAll
  static void start() throws InterruptedException {
    fakePlayer1 = UtilsForTesting.initFakePlayer("LastInstance");

    UtilsForTesting.waitUntilPlayerJoin(fakePlayer1);
  }

  @Test
  @Order(1)
  void testPlayerLastInstance() {
    final Instance lastPlayerInstance =
      CraftInstancesManager.get().getLastPlayerInstance(fakePlayer1);
    Assertions.assertNotNull(lastPlayerInstance, "assert that the player has last instance");
  }

  @Test
  @Order(2)
  void testPlayerDisconnectInstance() {
    fakePlayer1.getPlayerConnection().disconnect();
    EventDispatcher.call(new PlayerDisconnectEvent(fakePlayer1));
    final Instance lastPlayerInstance =
      CraftInstancesManager.get().getLastPlayerInstance(fakePlayer1);

    Assertions.assertNull(
      lastPlayerInstance, "assert that the last instance of the player was deleted");
  }

  @AfterAll
  static void kickPlayers() {
    fakePlayer1.kick("");
  }
}
