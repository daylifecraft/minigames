package com.daylifecraft.minigames

import com.daylifecraft.minigames.event.server.ServerStopEvent
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import net.minestom.server.MinecraftServer
import org.junit.jupiter.api.Test

internal class ShutdownHookTest {
  @Test
  fun test() {
    val spiedGlobalEventHandler = spyk(MinecraftServer.getGlobalEventHandler())

    mockkStatic(MinecraftServer::class) {
      every { MinecraftServer.getGlobalEventHandler() } returns spiedGlobalEventHandler

      ShutdownHook.global.run(1)

      verify(exactly = 1) { spiedGlobalEventHandler.call(ofType<ServerStopEvent>()) }
    }
  }
}
