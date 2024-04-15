package com.daylifecraft.minigames.games

import com.daylifecraft.common.config.ConfigFile
import com.daylifecraft.common.util.Range
import com.daylifecraft.minigames.Init
import com.daylifecraft.minigames.minigames.settings.GeneralGameSettings
import net.minestom.server.item.Material
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class MiniGamesSettingManagerTest {
  @Test
  @Order(1)
  fun testConfigsLoadedSuccessful() {
    val totalConfigsLoaded = Init.miniGamesSettingsManager.onStartupLoad()

    assertEquals(2, totalConfigsLoaded, "Not all configs were found/loaded")
  }

  @Test
  @Order(2)
  fun testSettingsLoaded() {
    assertNotNull(
      Init.miniGamesSettingsManager.getGeneralGameSettings("testMiniGame"),
      message = "testMiniGame config not found",
    )
    assertNotNull(
      Init.miniGamesSettingsManager.getGeneralGameSettings("testMiniGame2"),
      message = "testMiniGame2 config not found",
    )
  }

  @Test
  @Order(3)
  fun testInternalSettingsValues() {
    val settings = Init.miniGamesSettingsManager.getGeneralGameSettings("testMiniGame")

    assertEquals(
      expected = GeneralGameSettings(
        "testMiniGame",
        "games.testMiniGame.displayName",
        "games.testMiniGame.description",
        Material.RED_SAND,
        false,
        null,
        Range(1, 8),
        Range(1, 8),
        EMPTY_CONFIG,
      ),
      actual = settings,
      message = "TestMiniGame1 settings loaded not correct",
    )
  }

  @Test
  @Order(4)
  fun testInternalSettingsValues2() {
    val settings = Init.miniGamesSettingsManager.getGeneralGameSettings("testMiniGame2")

    assertEquals(
      expected = GeneralGameSettings(
        "testMiniGame2",
        "games.testMiniGame2.displayName",
        "games.testMiniGame2.description",
        Material.RED_SAND,
        false,
        "test.permission",
        Range(1, 8),
        Range(2, 6),
        EMPTY_CONFIG,
      ),
      actual = settings,
      message = "TestMiniGame2 settings loaded not correct",
    )
  }

  companion object {
    private val EMPTY_CONFIG = ConfigFile(emptyMap())
  }
}
