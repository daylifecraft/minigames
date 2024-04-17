package com.daylifecraft.common.util

/**
 * Divides receiver of this function by [divisor] rounding up.
 *
 * For example 2 [ceilDiv] 2 = 1
 * For example 2 [ceilDiv] 3 = 1
 * For example 3 [ceilDiv] 2 = 2
 */
infix fun Int.ceilDiv(divisor: Int): Int =
  this / divisor + if (this % divisor == 0) 0 else 1
