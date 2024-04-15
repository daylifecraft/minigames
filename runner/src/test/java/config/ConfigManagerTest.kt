package config

import com.daylifecraft.minigames.config.ConfigManager
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class ConfigManagerTest {

  @Test
  fun testLoadSuccess() {
    assertDoesNotThrow("ConfigManager.load() should not throw an exception") {
      ConfigManager.load()
    }
  }
}
