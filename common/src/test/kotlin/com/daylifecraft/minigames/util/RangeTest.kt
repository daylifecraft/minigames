package com.daylifecraft.minigames.util

import com.daylifecraft.common.util.Range
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val TEST_BORDER_VALUE = Range(1, 20)

internal class RangeTest {

  @ParameterizedTest
  @ValueSource(ints = [1, 2, 3, 4, 5, 20])
  fun testInBound(value: Int) {
    assertTrue(
      TEST_BORDER_VALUE.isInBorder(value),
      message = "Value must be in border",
    )
  }

  @ParameterizedTest
  @ValueSource(ints = [-1000, -1, 0])
  fun testClampMinValue(value: Int) {
    assertEquals(
      expected = TEST_BORDER_VALUE.minValue,
      actual = TEST_BORDER_VALUE.clamp(value),
      message = "Value must be min value",
    )
  }

  @ParameterizedTest
  @ValueSource(ints = [21, 1000])
  fun testClampMaxValue(value: Int) {
    assertEquals(
      expected = TEST_BORDER_VALUE.maxValue,
      actual = TEST_BORDER_VALUE.clamp(value),
      message = "Value must be max value",
    )
  }
}
