package com.daylifecraft.common.util.extensions

/**
 * Runs [runnable] if receiver is null and returns initial receiver for further processing.
 *
 * Behavior of this method is similar to elvis ?: operator
 * but it is not limited to do some terminal operation
 * e.g. return from function or some value of a whole statement
 *
 * @param runnable the block of code that will be run when the receiver is null
 * @param T type of the receiver
 * @receiver any object. Note that we can pass here non-nullable T, but it's fully unuseful
 */
inline infix fun <T> T?.runOnNull(runnable: () -> Unit): T? =
  also { it ?: runnable() }
