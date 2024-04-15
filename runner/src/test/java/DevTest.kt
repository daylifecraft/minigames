import com.daylifecraft.minigames.Dev
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DevTest {
  @Test
  fun testDev() {
    Dev.isDev = false
    assertFalse(Dev.isDev, "Dev.isDev() should return false")

    Dev.isDev = true
    assertTrue(Dev.isDev, "Dev.isDev() should return true")
  }
}
