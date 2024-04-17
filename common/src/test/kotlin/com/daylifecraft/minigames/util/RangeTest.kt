package com.daylifecraft.minigames.util

import com.daylifecraft.common.util.extensions.clamp
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals

private val TEST_RANGE = 1..20

internal class RangeTest {

  @ParameterizedTest
  @ValueSource(ints = [-1000, -1, 0])
  fun testClampMinValue(value: Int) {
    assertEquals(
      expected = TEST_RANGE.first,
      actual = TEST_RANGE.clamp(value),
      message = "Value must be min value",
    )
  }

  @ParameterizedTest
  @ValueSource(ints = [21, 1000])
  fun testClampMaxValue(value: Int) {
    assertEquals(
      expected = TEST_RANGE.last,
      actual = TEST_RANGE.clamp(value),
      message = "Value must be max value",
    )
  }
}
