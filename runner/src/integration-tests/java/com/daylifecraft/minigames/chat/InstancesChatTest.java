package com.daylifecraft.minigames.chat;

import com.daylifecraft.minigames.ChatManager;
import com.daylifecraft.minigames.Init;
import com.daylifecraft.minigames.instance.AbstractCraftInstance;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;

class InstancesChatTest {

  private static final ChatManager chatManager = Mockito.mock(ChatManager.class);

  private static final AbstractCraftInstance abstractCraftInstance =
    Mockito.mock(AbstractCraftInstance.class);

  private static final Instance instance = Mockito.mock(Instance.class);

  private static final Player fakePlayer1 = Mockito.mock(Player.class);
  private static final Player fakePlayer2 = Mockito.mock(Player.class);

  private static final String TEST_MESSAGE = "Some message";

  @BeforeAll
  static void setup() {
    Init.setupChatManager(chatManager);

    Mockito.doReturn(instance).when(abstractCraftInstance).getInstance();
    Mockito.doCallRealMethod()
      .when(abstractCraftInstance)
      .sendInstanceChatMessage(ArgumentMatchers.eq(fakePlayer1), ArgumentMatchers.anyString());
    Mockito.doReturn(true)
      .when(abstractCraftInstance)
      .canReceiveMessageFrom(ArgumentMatchers.any(), ArgumentMatchers.any());
  }

  @Test
  void testChatInOneInstance() {
    Mockito.doReturn(Sets.newSet(fakePlayer1, fakePlayer2)).when(instance).getPlayers();

    abstractCraftInstance.sendInstanceChatMessage(fakePlayer1, TEST_MESSAGE);

    Mockito.verify(chatManager)
      .sendPlayerChatMessage(
        ArgumentMatchers.eq(fakePlayer1),
        ArgumentMatchers.eq(fakePlayer2),
        ArgumentMatchers.anyString());
  }

  @Test
  void testChatInDifferentInstances() {
    Mockito.doReturn(Sets.newSet(fakePlayer1)).when(instance).getPlayers();

    abstractCraftInstance.sendInstanceChatMessage(fakePlayer1, TEST_MESSAGE);

    Mockito.verify(chatManager, Mockito.never())
      .sendPlayerChatMessage(
        ArgumentMatchers.eq(fakePlayer1),
        ArgumentMatchers.eq(fakePlayer2),
        ArgumentMatchers.anyString());
  }

  @AfterEach
  void clearInvocations() {
    Mockito.clearInvocations(chatManager);
  }
}
