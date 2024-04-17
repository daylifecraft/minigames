package com.daylifecraft.minigames.games

import com.daylifecraft.minigames.minigames.RoundPlayersSearcher.Companion.clampElementsListToBorder
import com.daylifecraft.minigames.minigames.queue.MiniGameQueueElement
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ClampingInRoundPlayersSearcherTest {
  @Test
  fun testMinGroupsClamping() {
    val playersBorder = 1..10
    assertEquals(
      expected = 1,
      actual = clampElementsListToBorder(queueElementList, playersBorder).size,
      message = "Expected that one group selected (priority to max players group)",
    )
  }

  @Test
  fun testMinGroupsClampingSize() {
    val playersBorder = 1..10
    assertEquals(
      expected = setOf(10),
      actual = clampElementsListToBorder(queueElementList, playersBorder)
        .map(MiniGameQueueElement::totalPlayersCount).toSet(),
      message = "Expected that one group selected & has 10 players",
    )
  }

  @Test
  fun testMaxGroupsClamping() {
    val playersBorder = 1..55
    assertEquals(
      expected = 10,
      actual = clampElementsListToBorder(queueElementList, playersBorder).size,
      message = "Expected that all 10 groups selected",
    )
  }

  @Test
  fun testMiddleGroupsClamping() {
    val playersBorder = 1..20
    assertEquals(
      expected = 3,
      actual = clampElementsListToBorder(queueElementList, playersBorder).size,
      message = "Expected that 3 groups selected",
    )
  }

  @Test
  fun testMiddleGroupsClampingSize() {
    val playersBorder = 1..20
    assertEquals(
      expected = listOf(1, 9, 10),
      actual = clampElementsListToBorder(queueElementList, playersBorder).stream()
        .map(MiniGameQueueElement::totalPlayersCount)
        .sorted()
        .toList(),
      message = "Expected that 3 groups selected",
    )
  }

  companion object {
    private val queueElementList: MutableList<MiniGameQueueElement> = ArrayList()

    @BeforeAll
    @JvmStatic
    fun setup() {
      // Filling list with groups of total players count: 1, 2, 3, ... 10
      for (playersCount in 1..10) {
        val mocked = mockk<MiniGameQueueElement>()
        every { mocked.totalPlayersCount } returns playersCount
        queueElementList.add(mocked)
      }
    }
  }
}
