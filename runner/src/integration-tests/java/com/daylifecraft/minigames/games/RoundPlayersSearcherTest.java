package com.daylifecraft.minigames.games;

import com.daylifecraft.minigames.minigames.RoundPlayersSearcher;
import com.daylifecraft.minigames.minigames.queue.MiniGameQueueElement;
import com.daylifecraft.common.util.JsonUtils;
import com.daylifecraft.common.util.Range;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoundPlayersSearcherTest {

  private static final List<MiniGameQueueElement> miniGameQueueElements = new ArrayList<>();

  @BeforeAll
  static void setup() {
    MiniGameQueueElement group = Mockito.mock(MiniGameQueueElement.class);
    Mockito.doReturn(10).when(group).getTotalPlayersCount();
    Mockito.doReturn(
        JsonUtils.getJsonObjectFromString(
          """
            {
            "maps": ["map1", "map2"]
            }
            """))
      .when(group)
      .getFilters();

    MiniGameQueueElement group2 = Mockito.mock(MiniGameQueueElement.class);
    Mockito.doReturn(2).when(group2).getTotalPlayersCount();
    Mockito.doReturn(
        JsonUtils.getJsonObjectFromString(
          """
            {
            "maps": ["map3", "map2"]
            }
            """))
      .when(group2)
      .getFilters();

    MiniGameQueueElement singlePlayer = Mockito.mock(MiniGameQueueElement.class);
    Mockito.doReturn(1).when(singlePlayer).getTotalPlayersCount();
    Mockito.doReturn(new JsonObject()).when(singlePlayer).getFilters();

    MiniGameQueueElement group3 = Mockito.mock(MiniGameQueueElement.class);
    Mockito.doReturn(4).when(group3).getTotalPlayersCount();
    Mockito.doReturn(
        JsonUtils.getJsonObjectFromString(
          """
            {
            "maps": ["map3", "map2"]
            }
            """))
      .when(group3)
      .getFilters();

    miniGameQueueElements.addAll(List.of(group, group2, group3, singlePlayer));
  }

  @Test
  void testMaxPlayersCombinationSize() {
    var resultCombination =
      RoundPlayersSearcher.findMaxPlayersCombination(
        miniGameQueueElements, new Range<>(1, 15));

    Assertions.assertEquals(3, resultCombination.size(), "Expected that combination size is 3");
  }

  @Test
  void testMaxPlayersCombination() {
    var resultCombination =
      RoundPlayersSearcher.findMaxPlayersCombination(
        miniGameQueueElements, new Range<>(1, 15));

    Assertions.assertIterableEquals(
      List.of(1, 4, 10),
      resultCombination.stream()
        .map(MiniGameQueueElement::getTotalPlayersCount)
        .sorted()
        .toList(),
      "Expected that combination of counts: 1 4 10");
  }
}
