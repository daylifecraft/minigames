package com.daylifecraft.common.util.extensions

/**
 * Clamps [value] to received range borders.
 */
fun <T : Comparable<T>> ClosedRange<T>.clamp(value: T): T = when {
  value > endInclusive -> endInclusive
  value < start -> start
  else -> value
}
