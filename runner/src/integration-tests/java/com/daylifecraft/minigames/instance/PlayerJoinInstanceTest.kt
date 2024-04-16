package com.daylifecraft.minigames.instance

import com.daylifecraft.common.instance.InstanceType
import com.daylifecraft.minigames.UtilsForTesting
import net.minestom.server.entity.Player
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class PlayerJoinInstanceTest {

  @ParameterizedTest
  @EnumSource(InstanceType::class)
  fun testPlayerSpawnPosition(instanceType: InstanceType) {
    if (!instanceType.isCreatedOnStartup) return

    val craftInstance = CraftInstancesManager.get().getAnyInstanceByType(instanceType)

    assertNotNull(craftInstance, "assert that instance not null")

    craftInstance.setupPlayer(fakePlayer)

    assertTrue(
      compareSpawnPosition(fakePlayer, craftInstance),
      message = "assert that fake player position is right position",
    )
  }

  private fun compareSpawnPosition(player: Player, instance: AbstractCraftInstance): Boolean {
    if (instance.doRememberPos()) {
      instance.loadPos(player.uuid)?.let {
        player.respawnPoint.distanceSquared(it) < 1
      }
    }
    return player.respawnPoint.distanceSquared(instance.getSpawnPos(player)) < 1
  }

  companion object {
    private val fakePlayer: Player = UtilsForTesting.initFakePlayer("InstanceTest")

    @BeforeAll
    @JvmStatic
    fun start() {
      UtilsForTesting.waitUntilPlayerJoin(fakePlayer)
    }

    @AfterAll
    @JvmStatic
    fun kickPlayers() {
      fakePlayer.kick("")
    }
  }
}
