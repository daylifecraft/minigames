package seasons

import com.daylifecraft.common.seasons.Season
import com.daylifecraft.common.seasons.SeasonDate
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

internal class SeasonTest {

  @Test
  fun testAttachmentOnSetActivenessValidation() {
    val season = Season("name", "displayName", SeasonDate("01.01"), SeasonDate("12.31"))

    assertFailsWith<IllegalStateException>("Season should be attached to list to perform setActiveness.") {
      season.setActiveness(Season.Activeness.FORCE_ACTIVE)
    }
  }

  @Test
  fun testAttachmentOnSetPriorityValidation() {
    val season = Season("name", "displayName", SeasonDate("01.01"), SeasonDate("12.31"))

    assertFailsWith<IllegalStateException>("Season should be attached to list to perform setPriority.") {
      season.priority = 10
    }
  }
}
