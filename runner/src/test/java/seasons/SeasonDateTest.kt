package seasons

import com.daylifecraft.common.seasons.SeasonDate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SeasonDateTest {

  @ParameterizedTest
  @ValueSource(strings = ["2004.12.01", "10", ""])
  fun testExactlyTwoTokens(dateString: String) {
    assertFailsWith<IllegalArgumentException>(message = "String should contain exactly 2 tokens") {
      SeasonDate(dateString)
    }
  }

  @ParameterizedTest
  @ValueSource(strings = ["13.10", "0.10"])
  fun testMonthValidation(dateString: String) {
    assertFailsWith<IllegalArgumentException>(message = "Month should be in [1, 12]") {
      SeasonDate(dateString)
    }
  }

  @ParameterizedTest
  @ValueSource(strings = ["2.32", "2.32"])
  fun testDayValidation(dateString: String) {
    assertFailsWith<IllegalArgumentException>(message = "Day should be in [1, 31]") {
      SeasonDate(dateString)
    }
  }

  @Test
  fun testCorrectDateCreation() {
    val date = SeasonDate("10.01")

    assertEquals(1, date.day, "Should be parsed correctly")
    assertEquals(10, date.month, "Should be parsed correctly")
  }

  @ParameterizedTest
  @ValueSource(strings = ["01.01", "12.31"])
  fun testCorrectDatesNotThrow(dateString: String) {
    assertDoesNotThrow {
      SeasonDate(dateString)
    }
  }

  @Test
  fun testDateComparators() {
    val firstDate = SeasonDate("01.01")
    val secondDate = SeasonDate("05.05")
    val thirdDate = SeasonDate("05.05")

    assertFalse(firstDate.greaterOrEqual(secondDate), "Actually it is less")
    assertTrue(firstDate.lessOrEqual(secondDate), "01.01 <= 05.05")

    assertTrue(secondDate.greaterOrEqual(firstDate), "05.05 >= 01.01")
    assertFalse(secondDate.lessOrEqual(firstDate), "Actually it is greater")

    assertTrue(secondDate.lessOrEqual(thirdDate), "They are equal")
    assertTrue(secondDate.greaterOrEqual(thirdDate), "They are equal")
  }

  @Test
  fun testDateIsBetween() {
    val firstDate = SeasonDate("01.01")
    val secondDate = SeasonDate("02.02")
    val thirdDate = SeasonDate("03.03")

    assertTrue(secondDate.isBetween(firstDate, thirdDate), "01.01 <= 02.02 <= 03.03")
    assertFalse(
      secondDate.isBetween(thirdDate, firstDate),
      "Time segment starts at 03.03 of this year and stops at 01.01 of next year",
    )

    assertTrue(
      firstDate.isBetween(thirdDate, secondDate),
      "Time segment starts at 03.03 of this year and stops at 02.02 of next year",
    )

    assertFalse(
      secondDate.isBetween(firstDate, firstDate),
      "01.01 - 01.01 considered one day not a whole year",
    )
    assertTrue(
      firstDate.isBetween(firstDate, firstDate),
      "01.01 - 01.01 considered one day not a whole year",
    )
  }
}
