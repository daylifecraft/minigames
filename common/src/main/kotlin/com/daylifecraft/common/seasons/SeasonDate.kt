package com.daylifecraft.common.seasons

import java.time.LocalDate
import java.util.Objects

/** Class representing a date of year.  */
class SeasonDate : Comparable<SeasonDate> {
  val month: Int
  val day: Int

  /**
   * Creates season date from string
   *
   * @param dateData string in format MM.DD
   */
  constructor(dateData: String) {
    val split = dateData.split('.')
    require(split.size == TOKENS_COUNT) { "Season date string should contains exactly two tokens separated by dot" }

    month = split[0].toInt()
    day = split[1].toInt()

    assertValidMonth(month)
    assertValidDay(day)
  }

  /**
   * Creates season date with provided month and day.
   *
   * @param month integer in [1, 31]
   * @param day integer in [1, 12]
   */
  constructor(month: Int, day: Int) {
    this.month = month
    this.day = day

    assertValidMonth(month)
    assertValidDay(day)
  }

  // TODO remove when usage side will be in kotlin
  fun greaterOrEqual(other: SeasonDate?): Boolean = compareTo(other!!) >= 0

  fun lessOrEqual(other: SeasonDate?): Boolean = compareTo(other!!) <= 0

  /** Checks if the date is in some time span. Note that start may not be less than end.  */
  fun isBetween(start: SeasonDate, end: SeasonDate?): Boolean = if (start.lessOrEqual(end)) {
    greaterOrEqual(start) && lessOrEqual(end)
  } else {
    greaterOrEqual(start) || lessOrEqual(end)
  }

  override fun compareTo(other: SeasonDate): Int {
    val byMonth = month.compareTo(other.month)

    if (byMonth != 0) {
      return byMonth
    }

    return day.compareTo(other.day)
  }

  override fun equals(other: Any?): Boolean {
    other ?: return false
    if (other is SeasonDate) {
      return month == other.month && day == other.day
    }
    return false
  }

  override fun hashCode(): Int = Objects.hash(month, day)

  override fun toString(): String = formatToken(month) + "." + formatToken(day)

  companion object {
    private const val TOKENS_COUNT = 2
    private const val MAX_DAYS_IN_MONTH = 31
    private const val MONTHS_IN_YEAR = 12
    private const val MIN_TWO_DIGITS_INT = 10

    fun current(): SeasonDate {
      val currentDate = LocalDate.now()
      return SeasonDate(currentDate.monthValue, currentDate.dayOfMonth)
    }

    private fun assertValidDay(day: Int) {
      require(!(day < 1 || day > MAX_DAYS_IN_MONTH)) { "Day should be in [1; 31]" }
    }

    private fun assertValidMonth(month: Int) {
      require(!(month < 1 || month > MONTHS_IN_YEAR)) { "Month should be in [1; 12]" }
    }

    private fun formatToken(tokenValue: Int): String = if (tokenValue < MIN_TWO_DIGITS_INT) {
      "0$tokenValue"
    } else {
      tokenValue.toString()
    }
  }
}
