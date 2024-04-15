package com.daylifecraft.minigames

import com.daylifecraft.common.TaskManager.removeAllTasks
import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.event.server.ServerStopEvent
import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventDispatcher
import sun.misc.Signal
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

/** Shutdown hook, uses for call ServerStopEvent and stop server clearly  */
class ShutdownHook {
  private val isShuttingDown = AtomicBoolean(false)

  /**
   * Call ServerStopEvent and shutdown server
   *
   * @param args special argument that give info about stop reason
   */
  fun run(args: Long) {
    // Check if server already shutting down
    if (isShuttingDown.getAndSet(true)) {
      return
    }

    // Check if run reason is server
    if (args == 0L) {
      LOGGER.debug("Server shutting down...")
    }

    // Call ServerStopEvent to process all listener's
    val stopEvent = ServerStopEvent(args)
    EventDispatcher.call(stopEvent)

    // Check shutdownHook run reason: 0 -> server, 1 -> test (dry-run)
    if (args != 0L) {
      isShuttingDown.set(false)
      return
    }

    // Remove all task's in TaskManager
    removeAllTasks()
    // Stop Minestom core
    MinecraftServer.stopCleanly()
    // Unload database
    DatabaseManager.unload()

    // VM exit
    exitProcess(0)
  }

  companion object {
    private val LOGGER = createLogger<ShutdownHook>()

    lateinit var global: ShutdownHook
      private set

    fun init() {
      global = ShutdownHook()
      Signal.handle(Signal("INT")) { _ -> global.run(0) }
      Signal.handle(Signal("TERM")) { _ -> global.run(0) }
    }
  }
}
