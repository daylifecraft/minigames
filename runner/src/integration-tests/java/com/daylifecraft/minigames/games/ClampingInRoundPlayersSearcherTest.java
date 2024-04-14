package com.daylifecraft.minigames.games;

import com.daylifecraft.minigames.minigames.RoundPlayersSearcher;
import com.daylifecraft.minigames.minigames.queue.MiniGameQueueElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.daylifecraft.common.util.Range;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ClampingInRoundPlayersSearcherTest {

  private static final List<MiniGameQueueElement> queueElementList = new ArrayList<>();

  @BeforeAll
  static void setup() {
    // Filling list with groups of total players count: 1, 2, 3, ... 10
    for (var playersCount = 1; playersCount <= 10; playersCount++) {
      var mocked = Mockito.mock(MiniGameQueueElement.class);
      Mockito.doReturn(playersCount).when(mocked).getTotalPlayersCount();
      queueElementList.add(mocked);
    }
  }

  @Test
  void testMinGroupsClamping() {
    Range<Integer> playersBorder = new Range<>(1, 10);
    Assertions.assertEquals(
      1,
      RoundPlayersSearcher.clampElementsListToBorder(queueElementList, playersBorder).size(),
      "Expected that one group selected (priority to max players group)");
  }

  @Test
  void testMinGroupsClampingSize() {
    Range<Integer> playersBorder = new Range<>(1, 10);
    Assertions.assertIterableEquals(
      Collections.singleton(10),
      RoundPlayersSearcher.clampElementsListToBorder(queueElementList, playersBorder).stream()
        .map(MiniGameQueueElement::getTotalPlayersCount)
        .toList(),
      "Expected that one group selected & has 10 players");
  }

  @Test
  void testMaxGroupsClamping() {
    Range<Integer> playersBorder = new Range<>(1, 55);
    Assertions.assertEquals(
      10,
      RoundPlayersSearcher.clampElementsListToBorder(queueElementList, playersBorder).size(),
      "Expected that all 10 groups selected");
  }

  @Test
  void testMiddleGroupsClamping() {
    Range<Integer> playersBorder = new Range<>(1, 20);
    Assertions.assertEquals(
      3,
      RoundPlayersSearcher.clampElementsListToBorder(queueElementList, playersBorder).size(),
      "Expected that 3 groups selected");
  }

  @Test
  void testMiddleGroupsClampingSize() {
    Range<Integer> playersBorder = new Range<>(1, 20);
    Assertions.assertIterableEquals(
      List.of(1, 9, 10),
      RoundPlayersSearcher.clampElementsListToBorder(queueElementList, playersBorder).stream()
        .map(MiniGameQueueElement::getTotalPlayersCount)
        .sorted()
        .toList(),
      "Expected that 3 groups selected");
  }
}
