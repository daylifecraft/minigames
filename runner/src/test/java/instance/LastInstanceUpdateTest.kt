package instance

import com.daylifecraft.minigames.Init.enableTests
import com.daylifecraft.minigames.Init.setupCraftInstancesManager
import com.daylifecraft.minigames.instance.AbstractCraftInstance
import com.daylifecraft.minigames.instance.CraftInstancesManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LastInstanceUpdateTest {
  @Test
  fun testDoesInstanceUpdatedOnSpawn() {
    craftInstancesManager.notifyPlayerSpawn(spawnInstance, player)

    assertEquals(
      expected = spawnInstance,
      actual = craftInstancesManager.getLastPlayerInstance(player),
      message = "Expected that last player instance updated on spawn",
    )
  }

  @Test
  fun testDoesInstanceRemovedOnDisconnect() {
    craftInstancesManager.notifyPlayerDisconnect(spawnInstance, player)

    assertNull(
      craftInstancesManager.getLastPlayerInstance(player),
      message = "Expected that last player instance removed on disconnect",
    )
  }

  companion object {
    private val craftInstancesManager = spyk(CraftInstancesManager(mockk(relaxed = true)))

    private val spawnInstance = mockk<Instance>(relaxed = true)
    private val abstractCraftInstance = mockk<AbstractCraftInstance>(relaxed = true)

    private val player = mockk<Player>()

    @BeforeAll
    @JvmStatic
    fun setup() {
      enableTests()
      setupCraftInstancesManager(craftInstancesManager)

      every { player.uuid } returns UUID.fromString("00000000-0000-0000-0000-000000000000")

      every { craftInstancesManager.getInstance(any()) } returns abstractCraftInstance
    }
  }
}
