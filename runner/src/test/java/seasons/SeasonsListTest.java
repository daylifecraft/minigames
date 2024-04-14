package seasons;

import com.daylifecraft.common.seasons.Season;
import com.daylifecraft.common.seasons.SeasonDate;
import com.daylifecraft.common.seasons.SeasonsList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

class SeasonsListTest {
  private static final String FIRST_SEASON_NAME = "mock-name";
  private static final SeasonDate SEASON_DATE = new SeasonDate(10, 10);
  private static final SeasonDate NOT_SEASON_DATE = new SeasonDate(1, 1);

  private static SeasonsList seasonsList;
  private static MockedStatic<SeasonDate> currentDateMock;

  @BeforeAll
  static void initSeasonsList() {
    List<Season> seasons = new ArrayList<>();

    seasons.add(new Season(FIRST_SEASON_NAME, "1", SEASON_DATE, SEASON_DATE));

    seasonsList = new SeasonsList(seasons);
  }

  @BeforeAll
  static void initMocks() {
    currentDateMock = Mockito.mockStatic(SeasonDate.class);
    currentDateMock.when(SeasonDate::current).thenReturn(NOT_SEASON_DATE);
  }

  @Test
  void testGetByName() {
    Season season = seasonsList.getSeasonByName(FIRST_SEASON_NAME);
    Assertions.assertNotNull(season, "It actually in the seasons list");
    Assertions.assertEquals(FIRST_SEASON_NAME, season.name, "Ensure that it has requested name");
  }

  @Test
  void testSeasonActivation() {
    Season season = seasonsList.getSeasonByName(FIRST_SEASON_NAME);

    Assertions.assertTrue(
      seasonsList.getActiveSeasonsPrioritized().isEmpty(), "First season should not be active");

    season.setActiveness(Season.Activeness.FORCE_ACTIVE);
    Assertions.assertEquals(
      1, seasonsList.getActiveSeasonsPrioritized().size(), "First season should be activated");

    season.setActiveness(Season.Activeness.FORCE_STOPPED);
    Assertions.assertTrue(
      seasonsList.getActiveSeasonsPrioritized().isEmpty(), "First season should not be active");
  }

  @AfterAll
  static void clearMocks() {
    currentDateMock.close();
  }
}
