package chat

import com.daylifecraft.minigames.Init
import com.daylifecraft.minigames.PermissionManager
import com.daylifecraft.minigames.util.ChatUtil.checkMiniMessage
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.entity.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private const val TEST_MESSAGE = "<b>Test</b> <blue>message</blue>"

internal class MiniMessageTest {

  @RelaxedMockK
  private lateinit var fakePlayer: Player

  @BeforeEach
  fun setup() {
    Init.enableTests()
  }

  @Test
  fun testMessageWasStripped() {
    assertEquals(
      expected = checkMiniMessage(fakePlayer, TEST_MESSAGE),
      actual = MiniMessage.miniMessage().stripTags(TEST_MESSAGE),
      message = "Expected that tags was removed for no permission player",
    )
  }

  @Test
  fun testMessage() {
    mockkObject(PermissionManager)
    every {
      PermissionManager.hasPermission(fakePlayer, "chat.minimessage.full")
    } returns true

    assertEquals(
      expected = TEST_MESSAGE,
      actual = checkMiniMessage(fakePlayer, TEST_MESSAGE),
      message = "Expected that tags was not removed!",
    )
  }
}
