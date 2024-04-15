package seasons;

import com.daylifecraft.common.seasons.Season;
import com.daylifecraft.common.seasons.SeasonDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SeasonTest {
  private Season season;

  @BeforeEach
  void initSeason() {
    season = new Season("name", "displayName", new SeasonDate("01.01"), new SeasonDate("12.31"));
  }

  @Test
  void testAttachmentOnSetActivenessValidation() {
    Assertions.assertThrows(
      IllegalStateException.class,
      () -> season.setActiveness(Season.Activeness.FORCE_ACTIVE),
      "Season should be attached to list before this.");
  }

  @Test
  void testAttachmentOnSetPriorityValidation() {
    Assertions.assertThrows(
      IllegalStateException.class,
      () -> season.setPriority(10),
      "Season should be attached to list before this.");
  }
}
