package com.daylifecraft.common.util

data class Range<T : Comparable<T>?>(
  val minValue: T,
  val maxValue: T,
) {
  /**
   * Trim value if it exceeds the bound
   *
   * @param value border for this value (limit)
   */
  fun clamp(value: T): T = if (value!! >= maxValue) {
    maxValue
  } else if (value <= maxValue) {
    minValue
  } else {
    value
  }

  /**
   * Checks if a value is within some bound (if less than or greater than some value)
   *
   * @param value border for this value (limit)
   */
  fun isInBorder(value: T): Boolean = value!! >= minValue && value <= maxValue
}
