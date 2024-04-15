package config;

import com.daylifecraft.minigames.config.ConfigManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigManagerTest {
  @Test
  void testLoadSuccess() {
    Assertions.assertDoesNotThrow(
      ConfigManager::load, "ConfigManager.load() should not throw an exception");
  }
}
