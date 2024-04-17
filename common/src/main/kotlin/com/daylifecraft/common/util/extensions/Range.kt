package com.daylifecraft.common.util.extensions

fun <T : Comparable<T>> ClosedRange<T>.clamp(value: T): T = when {
  value > endInclusive -> endInclusive
  value < start -> start
  else -> value
}
