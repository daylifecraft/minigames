package com.daylifecraft.minigames.games

import com.daylifecraft.common.util.JsonUtils
import com.daylifecraft.common.util.Range
import com.daylifecraft.minigames.minigames.RoundPlayersSearcher
import com.daylifecraft.minigames.minigames.queue.MiniGameQueueElement
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class RoundPlayersSearcherTest {

  @Test
  fun testMaxPlayersCombination() {
    val resultCombination = RoundPlayersSearcher.findMaxPlayersCombination(
      miniGameQueueElements,
      Range(1, 15),
    )

    assertEquals(
      expected = listOf(1, 4, 10),
      actual = resultCombination
        .map(MiniGameQueueElement::totalPlayersCount)
        .sorted(),
      message = "Expected that combination of counts: 1 4 10",
    )
  }

  companion object {
    private val miniGameQueueElements: MutableList<MiniGameQueueElement> = ArrayList()

    @BeforeAll
    @JvmStatic
    fun setup() {
      val group1 = mockk<MiniGameQueueElement>()
      every { group1.totalPlayersCount } returns 10
      every { group1.filters } returns JsonUtils.getJsonObjectFromString(
        """
            { "maps": ["map1", "map2"] }
        """.trimIndent(),
      )

      val group2 = mockk<MiniGameQueueElement>()
      every { group2.totalPlayersCount } returns 2
      every { group2.filters } returns JsonUtils.getJsonObjectFromString(
        """
            { "maps": ["map3", "map2"] }
        """.trimIndent(),
      )

      val group3 = mockk<MiniGameQueueElement>()
      every { group3.totalPlayersCount } returns 4
      every { group3.filters } returns JsonUtils.getJsonObjectFromString(
        """
            { "maps": ["map3", "map2"] }
        """.trimIndent(),
      )

      val singlePlayer = mockk<MiniGameQueueElement>()
      every { singlePlayer.totalPlayersCount } returns 1
      every { singlePlayer.filters } returns JsonObject()

      miniGameQueueElements.addAll(listOf(group1, group2, group3, singlePlayer))
    }
  }
}
