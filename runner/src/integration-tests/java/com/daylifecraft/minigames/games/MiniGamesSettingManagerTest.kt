package com.daylifecraft.minigames.games

import com.daylifecraft.common.config.ConfigFile
import com.daylifecraft.minigames.Init
import com.daylifecraft.minigames.minigames.settings.GeneralGameSettings
import net.minestom.server.item.Material
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private val EMPTY_CONFIG = ConfigFile(emptyMap())

internal class MiniGamesSettingManagerTest {
  @Test
  fun testConfigsLoadedSuccessful() {
    val totalConfigsLoaded = Init.miniGamesSettingsManager.onStartupLoad()

    assertEquals(2, totalConfigsLoaded, "Not all configs were found/loaded")
  }

  @Test
  fun testSettingsLoaded() {
    assertNotNull(
      Init.miniGamesSettingsManager.getGeneralGameSettings("testMiniGame"),
      message = "testMiniGame config not found",
    )
  }

  @Test
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
        1..8,
        1..8,
        EMPTY_CONFIG,
      ),
      actual = settings,
      message = "TestMiniGame1 settings loaded not correct",
    )
  }
}
