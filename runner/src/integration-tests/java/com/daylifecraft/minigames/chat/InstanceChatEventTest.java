package com.daylifecraft.minigames.chat;

import com.daylifecraft.minigames.ChatManager;
import com.daylifecraft.minigames.Init;
import com.daylifecraft.minigames.UtilsForTesting;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerChatEvent;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Collections;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InstanceChatEventTest {

  private static Player fakePlayer;

  private static final ChatManager chatManager = Mockito.mock(ChatManager.class);

  @BeforeAll
  static void start() throws InterruptedException {
    Init.setupChatManager(chatManager);

    fakePlayer = UtilsForTesting.initFakePlayer("EventsTest");

    UtilsForTesting.waitUntilPlayerJoin(fakePlayer);
  }

  @Test
  @Order(1)
  void testDoesEventCalled() {
    final PlayerChatEvent event =
      new PlayerChatEvent(fakePlayer, Collections.emptyList(), Component::empty, "Some text");
    EventDispatcher.call(event);

    Assertions.assertTrue(event.isCancelled(), "Event must me cancelled");

    Mockito.verify(chatManager)
      .sendPlayerChatMessage(
        ArgumentMatchers.eq(fakePlayer),
        ArgumentMatchers.eq(fakePlayer),
        ArgumentMatchers.anyString());
  }

  @AfterAll
  static void kickPlayer() {
    fakePlayer.kick("");
  }
}
