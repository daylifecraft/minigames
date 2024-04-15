package com.daylifecraft.common.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/** Class provides utils to work with MiniGame Filters in JsonObjects representation  */
object FilterUtils {
  /**
   * Forming result filter JsonObject using the method described in the documentation
   *
   * @param objects Filters objects
   * @return result filters
   */
  fun getResultFilters(vararg objects: JsonObject): JsonObject = getResultFilters(listOf(*objects))

  /**
   * Forming result filter JsonObject using the method described in the documentation
   *
   * @param objects Filters objects
   * @return result filters
   */
  fun getResultFilters(objects: List<JsonObject>): JsonObject {
    val finalFilters = JsonObject()

    val matrix: MutableMap<String, MutableList<JsonArray>> = HashMap()

    for (jsonObject in objects) {
      for ((key, value) in jsonObject.entrySet()) {
        if (!value.isJsonArray && !finalFilters.has(key)) {
          finalFilters.add(key, value)
          continue
        }

        if (!value.isJsonArray) {
          continue
        }

        matrix.computeIfAbsent(key) {
          mutableListOf()
        }.add(value.asJsonArray)
      }
    }

    for ((key, value) in matrix) {
      val resultArray = getFinalJsonArray(value)

      if (resultArray.isEmpty) continue

      finalFilters.add(key, resultArray)
    }

    return finalFilters
  }

  private fun getFinalJsonArray(matrix: List<JsonArray>): JsonArray {
    val requiredCount = matrix.size
    val countMeets: MutableMap<JsonElement, Int> = HashMap()
    for (array in matrix) {
      for (element in array) {
        countMeets[element] = countMeets.getOrDefault(element, 0) + 1
      }
    }

    val finalArray = JsonArray()
    for ((key, value) in countMeets) {
      if (requiredCount == value) {
        finalArray.add(key)
      }
    }

    return finalArray
  }
}
