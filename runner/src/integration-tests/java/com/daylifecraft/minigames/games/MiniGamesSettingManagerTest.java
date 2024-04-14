package com.daylifecraft.minigames.games;

import com.daylifecraft.minigames.Init;
import com.daylifecraft.common.config.ConfigFile;
import com.daylifecraft.minigames.minigames.settings.GeneralGameSettings;
import com.daylifecraft.common.util.Range;

import java.util.Collections;

import net.minestom.server.item.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MiniGamesSettingManagerTest {

  private static final ConfigFile EMPTY_CONFIG_SECTION = new ConfigFile(Collections.emptyMap());

  @Test
  @Order(1)
  void testConfigsLoadedSuccessful() {
    final int totalConfigsLoaded = Init.getMiniGamesSettingsManager().onStartupLoad();

    Assertions.assertEquals(2, totalConfigsLoaded, "Not all configs were found/loaded");
  }

  @Test
  @Order(2)
  void testSettingsLoaded() {
    Assertions.assertTrue(
      Init.getMiniGamesSettingsManager().getGeneralGameSettings("testMiniGame") != null
        && Init.getMiniGamesSettingsManager().getGeneralGameSettings("testMiniGame2") != null,
      "Configs was not loaded");
  }

  @Test
  @Order(3)
  void testInternalSettingsValues() {
    final var settings = Init.getMiniGamesSettingsManager().getGeneralGameSettings("testMiniGame");

    Assertions.assertEquals(
      new GeneralGameSettings(
        "testMiniGame",
        "games.testMiniGame.displayName",
        "games.testMiniGame.description",
        Material.RED_SAND,
        false,
        null,
        new Range<>(1, 8),
        new Range<>(1, 8),
        EMPTY_CONFIG_SECTION),
      settings,
      "TestMiniGame1 settings loaded not correct");
  }

  @Test
  @Order(4)
  void testInternalSettingsValues2() {
    final var settings = Init.getMiniGamesSettingsManager().getGeneralGameSettings("testMiniGame2");

    Assertions.assertEquals(
      new GeneralGameSettings(
        "testMiniGame2",
        "games.testMiniGame2.displayName",
        "games.testMiniGame2.description",
        Material.RED_SAND,
        false,
        "test.permission",
        new Range<>(1, 8),
        new Range<>(2, 6),
        EMPTY_CONFIG_SECTION),
      settings,
      "TestMiniGame2 settings loaded not correct");
  }
}
