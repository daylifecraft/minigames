package seasons

import com.daylifecraft.common.seasons.Season
import com.daylifecraft.common.seasons.SeasonDate
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

internal class SeasonTest {

  @Test
  fun testAttachmentOnSetActivenessValidation() {
    val season = Season("name", "displayName", SeasonDate("01.01"), SeasonDate("12.31"))

    assertFailsWith<IllegalStateException>(
      message = "Season should be attached to list before this.",
    ) {
      season.setActiveness(Season.Activeness.FORCE_ACTIVE)
    }
  }

  @Test
  fun testAttachmentOnSetPriorityValidation() {
    val season = Season("name", "displayName", SeasonDate("01.01"), SeasonDate("12.31"))

    assertFailsWith<IllegalStateException>(
      message = "Season should be attached to list before this.",
    ) {
      season.priority = 10
    }
  }
}
