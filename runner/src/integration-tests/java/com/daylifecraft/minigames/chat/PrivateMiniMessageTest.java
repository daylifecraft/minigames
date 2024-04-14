package com.daylifecraft.minigames.chat;

import com.daylifecraft.minigames.ChatManager;
import com.daylifecraft.minigames.Init;
import com.daylifecraft.minigames.UtilsForTesting;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PrivateMiniMessageTest {
  private static Player fakePlayer1, fakePlayer2;
  private static final String message = "<b>Test</b> <blue>message</blue>";

  private static final ChatManager chatManager = Mockito.mock(ChatManager.class);

  @BeforeAll
  static void start() throws InterruptedException {
    fakePlayer1 = UtilsForTesting.initFakePlayer("PMTest1");
    fakePlayer2 = UtilsForTesting.initFakePlayer("PMTest2");

    UtilsForTesting.waitUntilPlayerJoin(fakePlayer1, fakePlayer2);

    Init.setupChatManager(chatManager);
  }

  @Test
  @Order(1)
  void testPlayerFormattedMessage() {
    MinecraftServer.getCommandManager().execute(fakePlayer1, "w PMTest2 " + message);

    Mockito.verify(chatManager)
      .sendPrivateMessage(
        ArgumentMatchers.eq(fakePlayer1),
        ArgumentMatchers.eq(fakePlayer2),
        ArgumentMatchers.anyBoolean(),
        ArgumentMatchers.anyBoolean(),
        ArgumentMatchers.eq(MiniMessage.miniMessage().stripTags(message)));
  }

  @Test
  @Order(2)
  void testPlayerMessage() {
    fakePlayer1.addPermission(new Permission("isAdmin"));
    MinecraftServer.getCommandManager().execute(fakePlayer1, "w PMTest2 " + message);

    Mockito.verify(chatManager)
      .sendPrivateMessage(
        ArgumentMatchers.eq(fakePlayer1),
        ArgumentMatchers.eq(fakePlayer2),
        ArgumentMatchers.anyBoolean(),
        ArgumentMatchers.anyBoolean(),
        ArgumentMatchers.eq(message));
  }

  @AfterAll
  static void kickPlayers() {
    fakePlayer1.kick("");
    fakePlayer2.kick("");
  }

  @AfterEach
  void clearInvocations() {
    Mockito.clearInvocations(chatManager);
  }
}
