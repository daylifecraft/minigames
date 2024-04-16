package com.daylifecraft.minigames.util

import com.daylifecraft.common.util.FilterUtils
import com.daylifecraft.common.util.JsonUtils
import com.google.gson.JsonObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class FilterUtilsTest {

  private val maps123 =
    JsonUtils.getJsonObjectFromString(
      """
          {
            "maps": ["map1", "map2", "map3"]
          }
      """.trimIndent(),
    )

  private val maps23 =
    JsonUtils.getJsonObjectFromString(
      """
          {
            "maps": ["map3", "map2"]
          }
      """.trimIndent(),
    )

  private val maps3 =
    JsonUtils.getJsonObjectFromString(
      """
          {
            "maps": ["map3"]
          }
      """.trimIndent(),
    )

  private val maps4 =
    JsonUtils.getJsonObjectFromString(
      """
          {
            "maps": ["map4"]
          }
      """.trimIndent(),
    )

  @Test
  fun testArraysMerging() {
    assertEquals(
      expected = maps3,
      actual = FilterUtils.getResultFilters(maps123, maps23, maps3),
      message = "Expected that one map selected and merged from three filter-objects",
    )
  }

  @Test
  fun testMergeOnNoIdenticalOnes() {
    assertEquals(
      expected = JsonObject(),
      actual = FilterUtils.getResultFilters(maps123, maps23, maps3, maps4),
      message = "Expected empty filters object when no identical on all",
    )
  }

  @Test
  fun testPrimitivesInFilters() {
    val first =
      JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map1","map2"],
            "important-filter": false
          }
        """.trimIndent(),
      )

    val second =
      JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map2"]
          }
        """.trimIndent(),
      )

    assertEquals(
      expected = JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map2"],
            "important-filter": false
          }
        """.trimIndent(),
      ),
      actual = FilterUtils.getResultFilters(first, second),
      message = "Expected that primitives types are in result filters",
    )
  }
}
