package com.daylifecraft.minigames.listener

import com.daylifecraft.common.logging.building.LogBuilder
import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.logging.foundation.LogEvent
import com.daylifecraft.minigames.gui.listeners.InventoryCloseListener
import com.daylifecraft.minigames.gui.listeners.TickListener
import com.daylifecraft.minigames.instance.listeners.PlayerConfigurationListener
import com.daylifecraft.minigames.instance.listeners.PlayerDisconnectListener
import com.daylifecraft.minigames.instance.listeners.PlayerSpawnListener
import net.minestom.server.event.GlobalEventHandler

object ListenerManager {
  private val LISTENERS: List<Class<out Listener<*>>> =
    listOf<Class<out Listener<*>>>(
      // Players
      PlayerChatListener::class.java,
      PlayerLoadListener::class.java,
      PlayerPreLoginListener::class.java,
      PlayerQuitListener::class.java,
      PlayerSettingsChangedListener::class.java,
      // PlayerSkinInitListener::class.java,
      // Instances-system
      PlayerDisconnectListener::class.java,
      PlayerConfigurationListener::class.java,
      PlayerSpawnListener::class.java,
      // GUI-system
      InventoryCloseListener::class.java,
      TickListener::class.java,
      // MiniGame
      PlayerPreparationEndListener::class.java,
      RoundPreparationListener::class.java,
      PlayerRoundLeaveListener::class.java,
    )

  private val LOGGER = createLogger()

  /**
   * Load all event listeners into the handler
   *
   * @param handler main event handler
   */
  fun load(handler: GlobalEventHandler) {
    for (listenerClass in LISTENERS) {
      handler.tryAddListener(listenerClass)
    }
  }

  private fun GlobalEventHandler.tryAddListener(listenerClass: Class<out Listener<*>>) {
    try {
      val listener = listenerClass.getConstructor().newInstance()

      addListener(listener.eventClass) {
        listener.onCalled(listener.eventClass.cast(it))
      }
    } catch (e: Exception) {
      LOGGER.build(LogEvent.GENERAL_DEBUG) {
        message("Failed to load listener: " + listenerClass.name)
        detailsSection(LogBuilder.KEY_ADDITIONAL, "stackTrace", e.stackTrace)
        detailsSection(LogBuilder.KEY_ADDITIONAL, "throwable", e.message)
      }
    }
  }
}
