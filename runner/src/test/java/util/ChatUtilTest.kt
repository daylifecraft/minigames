package util

import com.daylifecraft.minigames.util.ChatUtil.replaceVariables
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ChatUtilTest {
  @Test
  fun testReplaceVariables() {
    val s =
      replaceVariables(
        "Hello $(name)$(suffix.last)",
        "name" to "v1val",
        "suffix.last" to "!!!",
      )

    assertEquals(
      expected = "Hello v1val!!!",
      actual = s,
      message = "Variables should be replaced",
    )
  }
}
