package com.daylifecraft.minigames.commands

import net.minestom.server.MinecraftServer
import net.minestom.server.timer.ExecutionType
import net.minestom.server.timer.SchedulerManager
import net.minestom.server.timer.TaskSchedule
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class DebugLagCommandTest {

  @Test
  fun testLagCommandSchedulesSomeTask() {
    val schedulerManager = mock<SchedulerManager>()

    Mockito.mockStatic(MinecraftServer::class.java, Mockito.CALLS_REAL_METHODS).use { mockedServer ->
      mockedServer.`when`<SchedulerManager> { MinecraftServer.getSchedulerManager() }.thenReturn(schedulerManager)

      MinecraftServer.getCommandManager().executeServerCommand("~ lag 0.1")

      verify(schedulerManager, times(1)).scheduleTask(
        any(),
        eq(TaskSchedule.tick(1)),
        eq(TaskSchedule.tick(1)),
        eq(ExecutionType.TICK_START),
      )
    }
  }
}
