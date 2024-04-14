package com.daylifecraft.minigames;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DevTest {
  @Test
  void testDev() {
    Dev.setDev(false);
    Assertions.assertFalse(Dev.isDev(), "Dev.isDev() should return false");

    Dev.setDev(true);
    Assertions.assertTrue(Dev.isDev(), "Dev.isDev() should return true");
  }
}
