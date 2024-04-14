package util;

import com.daylifecraft.common.util.FilterUtils;
import com.daylifecraft.common.util.JsonUtils;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FilterUtilsTest {

  @Test
  void testArraysMerging() {
    JsonObject first =
      JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map1", "map2", "map3"]
          }""");

    JsonObject second =
      JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map3", "map2"]
          }""");

    JsonObject third =
      JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map3"]
          }""");

    Assertions.assertEquals(
      JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map3"]
          }"""),
      FilterUtils.getResultFilters(first, second, third),
      "Expected that one map selected and merged from three filter-objects");
  }

  @Test
  void testMergeOnNoIdenticalOnes() {
    JsonObject first =
      JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map1", "map2", "map3"]
          }""");

    JsonObject second =
      JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map3", "map2"]
          }""");

    JsonObject third =
      JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map3"]
          }""");

    JsonObject fourth =
      JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map4"]
          }""");

    Assertions.assertEquals(
      new JsonObject(),
      FilterUtils.getResultFilters(first, second, third, fourth),
      "Expected empty filters object when no identical on all");
  }

  @Test
  void testPrimitivesInFilters() {
    JsonObject first =
      JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map1","map2"],
            "important-filter": false
          }""");

    JsonObject second =
      JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map2"]
          }""");

    Assertions.assertEquals(
      JsonUtils.getJsonObjectFromString(
        """
          {
            "maps": ["map2"],
            "important-filter": false
          }"""),
      FilterUtils.getResultFilters(first, second),
      "Expected that primitives types are in result filters");
  }
}
