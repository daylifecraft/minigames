package com.daylifecraft.common.util

import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant

object TimeUtil {
  const val MILLIS_IN_A_SECOND = 1000L

  /** Get current seconds since 1970 year (in UTC TimeZone!)  */
  fun currentUtcSeconds(): Long =
    Instant.now().epochSecond

  /**
   * Create a formatted string from seconds
   *
   * @param seconds seconds we need to convert to string
   * @return formatted string
   */
  fun formatSecondsToString(seconds: Long): String =
    Timestamp.from(Instant.ofEpochSecond(seconds)).toString()

  /**
   * Creates a formatted string of seconds in a specialized format.
   *
   * @param seconds seconds we need to convert to string
   * @param format format for seconds
   * @return formatted string
   */
  fun formatSeconds(seconds: Long, format: String): String =
    SimpleDateFormat(format).format(Date(seconds * MILLIS_IN_A_SECOND))

  /**
   * Get elapsed seconds by time
   *
   * @param seconds seconds
   * @return how much time has passed
   */
  fun getElapsedSecondsByTime(seconds: Long): Long =
    currentUtcSeconds() - seconds
}
