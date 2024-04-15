package com.daylifecraft.minigames.chat;

import com.daylifecraft.common.instance.InstanceType;
import com.daylifecraft.minigames.ChatManager;
import com.daylifecraft.minigames.Init;
import com.daylifecraft.minigames.UtilsForTesting;
import com.daylifecraft.minigames.instance.CraftInstancesManager;
import com.daylifecraft.minigames.profile.group.PlayersGroup;
import com.daylifecraft.minigames.profile.group.PlayersGroupManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.permission.Permission;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GroupMiniMessageTest {
  private static Player fakePlayer1, fakePlayer2;
  private static PlayersGroup playersGroup;
  private static final String message = "<b>Test</b> <blue>message</blue>";

  private static final ChatManager chatManager = Mockito.mock(ChatManager.class);

  @BeforeAll
  static void start() throws InterruptedException {
    final Instance testInstance =
      CraftInstancesManager.get().getAnyInstanceByType(InstanceType.LOBBY).getInstance();
    fakePlayer1 = UtilsForTesting.initFakePlayer("GMTest1", testInstance);
    fakePlayer2 = UtilsForTesting.initFakePlayer("GMTest2", testInstance);

    UtilsForTesting.waitUntilPlayerJoin(fakePlayer1, fakePlayer2);

    playersGroup = PlayersGroupManager.createGroup(fakePlayer1.getUuid(), fakePlayer2.getUuid());

    Init.setupChatManager(chatManager);
  }

  @Test
  @Order(1)
  void testPlayerFormattedMessage() {
    MinecraftServer.getCommandManager().execute(fakePlayer1, "g " + message);
    Mockito.verify(chatManager)
      .sendGroupChatMessage(
        ArgumentMatchers.eq(fakePlayer1),
        ArgumentMatchers.eq(playersGroup),
        ArgumentMatchers.eq(MiniMessage.miniMessage().stripTags(message)));
  }

  @Test
  @Order(2)
  void testPlayerMessage() {
    fakePlayer1.addPermission(new Permission("isAdmin"));
    MinecraftServer.getCommandManager().execute(fakePlayer1, "g " + message);
    Mockito.verify(chatManager)
      .sendGroupChatMessage(
        ArgumentMatchers.eq(fakePlayer1),
        ArgumentMatchers.eq(playersGroup),
        ArgumentMatchers.eq(message));
  }

  @AfterAll
  static void kickPlayers() {
    fakePlayer1.kick("");
    fakePlayer2.kick("");
  }
}
