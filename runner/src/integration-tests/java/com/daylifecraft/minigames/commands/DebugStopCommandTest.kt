package com.daylifecraft.minigames.commands

import com.daylifecraft.minigames.ShutdownHook
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import net.minestom.server.MinecraftServer
import org.junit.jupiter.api.Test

internal class DebugStopCommandTest {
  @Test
  fun testStopDryRun() {
    val shutdownHookMock = mockk<ShutdownHook>(relaxed = true)
    mockkObject(ShutdownHook) {
      every { ShutdownHook.global } returns shutdownHookMock

      MinecraftServer.getCommandManager().executeServerCommand("~ stop dry-run")

      verify(exactly = 1) { shutdownHookMock.run(1) }
    }
  }

  @Test
  fun testStopRun() {
    val shutdownHookMock = mockk<ShutdownHook>(relaxed = true)
    mockkObject(ShutdownHook) {
      every { ShutdownHook.global } returns shutdownHookMock

      MinecraftServer.getCommandManager().executeServerCommand("~ stop")

      verify(exactly = 1) { shutdownHookMock.run(0) }
    }
  }
}
