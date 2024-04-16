package com.daylifecraft.minigames.instance

import com.daylifecraft.common.instance.InstanceType
import com.daylifecraft.minigames.instance.CraftInstancesManager.Companion.get
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class InstanceLobbyTest {
  @Test
  fun testLobbyInstanceRegistration() {
    assertTrue(lobbyInstance.isRegistered, "assert that lobby is registered")
  }

  @Test
  fun testLobbyWorldIsNotNull() {
    assertNotNull(spawnChunk, "assert that spawn chunk is not null")
  }

  @Test
  fun testLobbyWorldIsNotEmpty() {
    assertFalse(isChunkEmpty(spawnChunk), "assert that spawn chunk is not empty")
  }

  @Test
  fun testLobbyWorldChunkSupplier() {
    assertIs<LightingChunk>(spawnChunk, "assert that lobby chunks is lighting chunks")
  }

  private fun isChunkEmpty(chunk: Chunk): Boolean =
    chunk.sections.all { it.blockPalette().count() == 0 }

  companion object {
    private lateinit var lobbyInstance: Instance
    private lateinit var spawnChunk: Chunk

    @BeforeAll
    @JvmStatic
    fun start() {
      val lobbyCraftInstance = get().getAnyInstanceByType(InstanceType.LOBBY)
      lobbyInstance = lobbyCraftInstance!!.instance

      val pos = lobbyCraftInstance.getSpawnPos(null)
      val chunkX = pos.chunkX()
      val chunkZ = pos.chunkZ()

      spawnChunk = lobbyInstance.loadChunk(chunkX, chunkZ).join()
    }

    @AfterAll
    @JvmStatic
    fun finish() {
      lobbyInstance.unloadChunk(spawnChunk)
    }
  }
}
