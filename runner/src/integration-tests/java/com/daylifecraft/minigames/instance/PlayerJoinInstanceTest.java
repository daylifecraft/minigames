package com.daylifecraft.minigames.instance;

import com.daylifecraft.common.instance.InstanceType;
import com.daylifecraft.minigames.UtilsForTesting;
import net.minestom.server.entity.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PlayerJoinInstanceTest {

  private static Player fakePlayer;

  @BeforeAll
  static void start() throws InterruptedException {
    fakePlayer = UtilsForTesting.initFakePlayer("InstanceTest");

    UtilsForTesting.waitUntilPlayerJoin(fakePlayer);
  }

  @ParameterizedTest
  @EnumSource(InstanceType.class)
  void testPlayerSpawnPosition(final InstanceType instanceType) {
    if (!instanceType.isCreatedOnStartup()) {
      return;
    }

    final AbstractCraftInstance lobbyCraftInstance =
      CraftInstancesManager.get().getAnyInstanceByType(instanceType);
    Assertions.assertNotNull(lobbyCraftInstance, "assert that instance not null");
    if (fakePlayer.getInstance() != lobbyCraftInstance.getInstance()) {
      fakePlayer.setInstance(lobbyCraftInstance.getInstance());
    }
    fakePlayer.setRespawnPoint(lobbyCraftInstance.getSpawnPos(fakePlayer));
    if (lobbyCraftInstance.doRememberPos()
      && lobbyCraftInstance.loadPos(fakePlayer.getUuid()) != null) {
      fakePlayer.setRespawnPoint(lobbyCraftInstance.loadPos(fakePlayer.getUuid()));
    }
    Assertions.assertTrue(
      compareSpawnPosition(fakePlayer, lobbyCraftInstance),
      "assert that fake player position is right position");
  }

  private boolean compareSpawnPosition(final Player player, final AbstractCraftInstance instance) {
    if (instance.doRememberPos()) {
      if (instance.loadPos(player.getUuid()) != null) {
        return player.getRespawnPoint().blockX() == instance.loadPos(player.getUuid()).blockX()
          && player.getRespawnPoint().blockY() == instance.loadPos(player.getUuid()).blockY()
          && player.getRespawnPoint().blockZ() == instance.loadPos(player.getUuid()).blockZ();
      }
    }
    return player.getRespawnPoint().blockX() == instance.getSpawnPos(player).blockX()
      && player.getRespawnPoint().blockY() == instance.getSpawnPos(player).blockY()
      && player.getRespawnPoint().blockZ() == instance.getSpawnPos(player).blockZ();
  }

  @AfterAll
  static void kickPlayers() {
    fakePlayer.kick("");
  }
}
