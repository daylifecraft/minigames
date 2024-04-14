package com.daylifecraft.common.util

infix fun Int.ceilDiv(divisor: Int): Int = this / divisor + if (this % divisor == 0) 0 else 1
