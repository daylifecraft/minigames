package com.daylifecraft.minigames.minigames.controllers

import com.daylifecraft.common.logging.building.LogBuilder
import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.logging.foundation.LogEvent
import com.daylifecraft.minigames.Init.miniGameControllersManager
import com.daylifecraft.minigames.event.minigame.RoundPreStartEvent
import com.daylifecraft.minigames.event.player.minigame.PlayerPreparationEvent
import com.daylifecraft.minigames.event.player.minigame.PlayerRoundJoinEvent
import com.daylifecraft.minigames.minigames.settings.MiniGamesSettingManager
import com.daylifecraft.minigames.util.ReflectionUtils.getLoadedConvertedClassesFromPackage
import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventListener

/** Manager acts as an intermediary between a specific MiniGame controller and events  */
class MiniGameControllersManager(
  private val miniGamesSettingManager: MiniGamesSettingManager,
) {
  private val miniGameControllers: MutableMap<String, AbstractMiniGameController> = HashMap()
  private val events: MutableList<EventListener<*>> = ArrayList()

  /** Startup load. Init instances & listeners. Should be called on server starting  */
  fun onStartupLoad() {
    loadInstances()

    loadListeners()

    for (eventListener in events) {
      MinecraftServer.getGlobalEventHandler().addListener(eventListener)
    }
  }

  private fun loadInstances() {
    for (clazz in getLoadedConvertedClassesFromPackage(
      CONTROLLERS_PACKAGE,
      AbstractMiniGameController::class.java,
    )) {
      registerMiniGameController(clazz)
    }
  }

  /**
   * Register MiniGame Controller by class. It needs to have constructor: AbstractMiniGameController(MiniGameSettingManager.class)
   * @param miniGameControllerClass MiniGame controller class
   */
  private fun registerMiniGameController(miniGameControllerClass: Class<out AbstractMiniGameController>) {
    try {
      val constructor =
        miniGameControllerClass.getConstructor(
          MiniGamesSettingManager::class.java,
        )
      val newInstance = constructor.newInstance(miniGamesSettingManager)

      miniGameControllers[newInstance.miniGameId] = newInstance
    } catch (e: Exception) {
      createLogger().build(LogEvent.GENERAL_DEBUG) {
        message("Failed to load MiniGame controller: " + miniGameControllerClass.name)
        detailsSection(LogBuilder.KEY_ADDITIONAL, "stackTrace", e.stackTrace)
        detailsSection(LogBuilder.KEY_ADDITIONAL, "throwable", e.message)
      }
    }
  }

  private fun loadListeners() {
    events.add(
      object : EventListener<PlayerPreparationEvent> {
        override fun eventType(): Class<PlayerPreparationEvent> = PlayerPreparationEvent::class.java

        override fun run(event: PlayerPreparationEvent): EventListener.Result {
          val miniGameController =
            getMiniGameController(event.miniGameId)
              ?: return EventListener.Result.SUCCESS

          miniGameController.onPlayerPreparationEvent(event)
          return EventListener.Result.SUCCESS
        }
      },
    )

    events.add(
      object : EventListener<RoundPreStartEvent> {
        override fun eventType(): Class<RoundPreStartEvent> = RoundPreStartEvent::class.java

        override fun run(event: RoundPreStartEvent): EventListener.Result {
          val miniGameController =
            getMiniGameController(event.miniGameId)
              ?: return EventListener.Result.SUCCESS

          miniGameController.onRoundStartEvent(
            event.roundProfile,
            event.roundPlayerSettings,
            event.playersSpreadByTeams,
            event.finalGameFilters,
          )
          return EventListener.Result.SUCCESS
        }
      },
    )

    events.add(
      object : EventListener<PlayerRoundJoinEvent> {
        override fun eventType(): Class<PlayerRoundJoinEvent> = PlayerRoundJoinEvent::class.java

        override fun run(event: PlayerRoundJoinEvent): EventListener.Result {
          event.miniGameInstance
            .onPlayerRoundJoinEvent(event.player, event.playerSettings)

          return EventListener.Result.SUCCESS
        }
      },
    )
  }

  fun getMiniGameController(miniGameId: String): AbstractMiniGameController? = miniGameControllers[miniGameId]

  companion object {
    private val CONTROLLERS_PACKAGE = MiniGameControllersManager::class.java.packageName + ".games"

    /**
     * Returns instance from Init
     *
     * @return MiniGameControllersManager object
     */
    fun get(): MiniGameControllersManager = miniGameControllersManager
  }
}
