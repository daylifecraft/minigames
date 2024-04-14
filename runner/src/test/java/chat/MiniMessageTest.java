package chat;

import com.daylifecraft.minigames.Init;
import com.daylifecraft.minigames.PermissionManager;
import com.daylifecraft.minigames.util.ChatUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.entity.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class MiniMessageTest {

  private static final String TEST_MESSAGE = "<b>Test</b> <blue>message</blue>";

  private static Player fakePlayer;

  @BeforeAll
  static void setup() {
    Init.enableTests();

    fakePlayer = Mockito.mock(Player.class);
  }

  @Test
  void testMessageWasStripped() {
    Assertions.assertEquals(
      ChatUtil.checkMiniMessage(fakePlayer, TEST_MESSAGE),
      MiniMessage.miniMessage().stripTags(TEST_MESSAGE),
      "Expected that tags was removed for no permission player");
  }

  @Test
  void testMessage() {
    try (MockedStatic<PermissionManager> permissionManager =
           Mockito.mockStatic(PermissionManager.class)) {
      permissionManager
        .when(
          () ->
            PermissionManager.hasPermission(
              ArgumentMatchers.eq(fakePlayer),
              ArgumentMatchers.eq("chat.minimessage.full")))
        .thenReturn(true);

      Assertions.assertEquals(
        TEST_MESSAGE,
        ChatUtil.checkMiniMessage(fakePlayer, TEST_MESSAGE),
        "Expected that tags was not removed!");
    }
  }
}
