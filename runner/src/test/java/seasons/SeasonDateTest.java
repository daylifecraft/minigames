package seasons;

import com.daylifecraft.common.seasons.SeasonDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SeasonDateTest {

  @ParameterizedTest
  @ValueSource(strings = {"2004.12.01", "10", ""})
  void testExactlyTwoTokens(String dateString) {
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> new SeasonDate(dateString),
      "String should contain exactly 2 tokens");
  }

  @ParameterizedTest
  @ValueSource(strings = {"13.10", "0.10"})
  void testMonthValidation(String dateString) {
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> new SeasonDate(dateString),
      "Month should be in [1, 12]");
  }

  @ParameterizedTest
  @ValueSource(strings = {"2.32", "2.32"})
  void testDayValidation(String dateString) {
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> new SeasonDate(dateString),
      "Day should be in [1, 31]");
  }

  @Test
  void testCorrectDateCreation() {
    SeasonDate date =
      Assertions.assertDoesNotThrow(() -> new SeasonDate("10.01"), "Should be fine");

    Assertions.assertEquals(1, date.getDay(), "Should be parsed correctly");
    Assertions.assertEquals(10, date.getMonth(), "Should be parsed correctly");
  }

  @ParameterizedTest
  @ValueSource(strings = {"01.01", "12.31"})
  void testCorrectDatesNotThrow(String dateString) {
    Assertions.assertDoesNotThrow(() -> new SeasonDate(dateString), "Should be fine");
  }

  @Test
  void testDateComparators() {
    SeasonDate firstDate = new SeasonDate("01.01");
    SeasonDate secondDate = new SeasonDate("05.05");
    SeasonDate thirdDate = new SeasonDate("05.05");

    Assertions.assertFalse(firstDate.greaterOrEqual(secondDate), "Actually it is less");
    Assertions.assertTrue(firstDate.lessOrEqual(secondDate), "01.01 <= 05.05");

    Assertions.assertTrue(secondDate.greaterOrEqual(firstDate), "05.05 >= 01.01");
    Assertions.assertFalse(secondDate.lessOrEqual(firstDate), "Actually it is greater");

    Assertions.assertTrue(secondDate.lessOrEqual(thirdDate), "They are equal");
    Assertions.assertTrue(secondDate.greaterOrEqual(thirdDate), "They are equal");
  }

  @Test
  void testDateIsBetween() {
    SeasonDate firstDate = new SeasonDate("01.01");
    SeasonDate secondDate = new SeasonDate("02.02");
    SeasonDate thirdDate = new SeasonDate("03.03");

    Assertions.assertTrue(secondDate.isBetween(firstDate, thirdDate), "01.01 <= 02.02 <= 03.03");
    Assertions.assertFalse(
      secondDate.isBetween(thirdDate, firstDate),
      "Time segment starts at 03.03 of this year and stops at 01.01 of next year");

    Assertions.assertTrue(
      firstDate.isBetween(thirdDate, secondDate),
      "Time segment starts at 03.03 of this year and stops at 02.02 of next year");

    Assertions.assertFalse(
      secondDate.isBetween(firstDate, firstDate),
      "01.01 - 01.01 considered one day not a whole year");
    Assertions.assertTrue(
      firstDate.isBetween(firstDate, firstDate),
      "01.01 - 01.01 considered one day not a whole year");
  }
}
