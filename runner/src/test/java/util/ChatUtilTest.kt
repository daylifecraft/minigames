package util

import com.daylifecraft.minigames.util.ChatUtil.replaceVariables
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ChatUtilTest {
  @Test
  fun testReplaceVariables() {
    val s =
      replaceVariables(
        "Hello $(name)$(suffix.last)",
        "name" to "v1val",
        "suffix.last" to "!!!",
      )
    Assertions.assertEquals("Hello v1val!!!", s, "Variables should be replaced")
  }
}
