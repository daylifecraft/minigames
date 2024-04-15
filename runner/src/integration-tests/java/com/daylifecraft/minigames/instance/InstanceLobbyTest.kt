package com.daylifecraft.minigames.instance;

import com.daylifecraft.common.instance.InstanceType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.Section;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InstanceLobbyTest {

  private static Instance lobbyInstance;
  private static Chunk spawnChunk;

  @BeforeAll
  static void start() {
    final AbstractCraftInstance lobbyCraftInstance =
      CraftInstancesManager.get().getAnyInstanceByType(InstanceType.LOBBY);
    lobbyInstance = lobbyCraftInstance.getInstance();

    final Pos pos = lobbyCraftInstance.getSpawnPos(null);
    int chunkX = (int) Math.floor(pos.x() / 16);
    int chunkZ = (int) Math.floor(pos.z() / 16);

    lobbyInstance.loadChunk(chunkX, chunkZ);
    spawnChunk = lobbyInstance.getChunk(chunkX, chunkZ);
  }

  @Test
  void testLobbyInstanceRegistration() {
    Assertions.assertTrue(lobbyInstance.isRegistered(), "assert that lobby is registered");
  }

  @Test
  void testLobbyWorldIsNotNull() {
    Assertions.assertNotNull(spawnChunk, "assert that spawn chunk is not null");
  }

  @Test
  void testLobbyWorldIsNotEmpty() {
    Assertions.assertFalse(isChunkEmpty(spawnChunk), "assert that spawn chunk is not empty");
  }

  @Test
  void testLobbyWorldChunkSupplier() {
    Assertions.assertInstanceOf(
      LightingChunk.class, spawnChunk, "assert that lobby chunks is lighting chunks");
  }

  @AfterAll
  static void finish() {
    lobbyInstance.unloadChunk(spawnChunk);
  }

  public boolean isChunkEmpty(final Chunk chunk) {
    for (final Section section : chunk.getSections()) {
      if (section.blockPalette().count() > 0) {
        return false;
      }
    }
    return true;
  }
}
