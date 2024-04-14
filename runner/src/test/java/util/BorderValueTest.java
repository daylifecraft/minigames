package util;

import com.daylifecraft.common.util.Range;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BorderValueTest {

  private static final Range<Integer> TEST_BORDER_VALUE = new Range<>(1, 20);

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 4, 5, 20})
  void testInBound(final int value) {
    Assertions.assertTrue(TEST_BORDER_VALUE.isInBorder(value), "Value must be in border");
  }

  @ParameterizedTest
  @ValueSource(ints = {-1000, -1, 0})
  void testClampMinValue(final int value) {
    Assertions.assertEquals(
      TEST_BORDER_VALUE.minValue, TEST_BORDER_VALUE.clamp(value), "Value must be min value");
  }

  @ParameterizedTest
  @ValueSource(ints = {21, 1000})
  void testClampMaxValue(final int value) {
    Assertions.assertEquals(
      TEST_BORDER_VALUE.maxValue, TEST_BORDER_VALUE.clamp(value), "Value must be max value");
  }
}
