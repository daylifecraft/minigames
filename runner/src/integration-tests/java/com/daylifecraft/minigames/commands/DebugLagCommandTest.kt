package com.daylifecraft.minigames.commands

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.ExecutionType
import net.minestom.server.timer.SchedulerManager
import net.minestom.server.timer.TaskSchedule
import org.junit.jupiter.api.Test

class DebugLagCommandTest {

  @Test
  fun testLagCommandSchedulesSomeTask() {
    val schedulerManager = mockk<SchedulerManager>()

    mockkStatic(MinecraftServer::class) {
      every { MinecraftServer.getSchedulerManager() } returns schedulerManager

      MinecraftServer.getCommandManager().executeServerCommand("~ lag 0.1")

      verify(exactly = 1) {
        schedulerManager.scheduleTask(
          any(),
          eq(TaskSchedule.tick(1)),
          eq(TaskSchedule.tick(1)),
          eq(ExecutionType.TICK_START),
        )
      }
    }
  }
}
