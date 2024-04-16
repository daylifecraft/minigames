package seasons

import com.daylifecraft.common.seasons.Season
import com.daylifecraft.common.seasons.SeasonDate
import com.daylifecraft.common.seasons.SeasonsList
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val FIRST_SEASON_NAME = "mock-name"
private val SEASON_DATE = SeasonDate(10, 10)
private val NOT_SEASON_DATE = SeasonDate(1, 1)

internal class SeasonsListTest {

  private val seasonsList = SeasonsList(
    listOf(
      Season(FIRST_SEASON_NAME, "1", SEASON_DATE, SEASON_DATE),
    ),
  )

  @Test
  fun testGetByName() {
    mockkObject(SeasonDate) {
      every { SeasonDate.current() } returns NOT_SEASON_DATE

      val season = seasonsList.getSeasonByName(FIRST_SEASON_NAME)
      assertNotNull(season, "It actually in the seasons list")
      assertEquals(FIRST_SEASON_NAME, season.name, "Ensure that it has requested name")
    }
  }

  @Test
  fun testSeasonActivation() {
    mockkObject(SeasonDate) {
      every { SeasonDate.current() } returns NOT_SEASON_DATE

      val season = seasonsList.getSeasonByName(FIRST_SEASON_NAME)

      assertNotNull(season, "$FIRST_SEASON_NAME must be in the season list")

      assertTrue(
        seasonsList.activeSeasonsPrioritized.isEmpty(),
        message = "First season should not be active",
      )

      season.setActiveness(Season.Activeness.FORCE_ACTIVE)
      assertEquals(
        expected = 1,
        actual = seasonsList.activeSeasonsPrioritized.size,
        message = "First season should be activated",
      )

      season.setActiveness(Season.Activeness.FORCE_STOPPED)
      assertTrue(
        seasonsList.activeSeasonsPrioritized.isEmpty(),
        message = "First season should not be active",
      )
    }
  }
}
