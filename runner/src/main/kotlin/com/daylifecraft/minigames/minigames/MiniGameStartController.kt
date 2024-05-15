package com.daylifecraft.minigames.minigames

import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.minigames.PlayerManager.getPlayerByUuid
import com.daylifecraft.minigames.config.ConfigManager.mainConfig
import com.daylifecraft.minigames.database.DatabaseManager.addProfile
import com.daylifecraft.minigames.event.minigame.RoundPreStartEvent
import com.daylifecraft.minigames.gui.GuiItem
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager.getMiniGameQueueElement
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager.removeFromSearchQueueAndUnlockGroup
import com.daylifecraft.minigames.minigames.gui.ConfirmationMiniGameGui.Companion.showConfirmationGui
import com.daylifecraft.minigames.minigames.profile.RoundProfile.Companion.createNewRound
import com.daylifecraft.minigames.minigames.settings.GeneralGameSettings
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import com.daylifecraft.minigames.util.GuiUtil.isNotLR
import com.google.gson.JsonObject
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import net.minestom.server.timer.TaskSchedule
import java.util.UUID

/**
 * This class is responsible for starting the round. Creates an object in the database, calls the
 * event and controls the confirmation GUI
 */
class MiniGameStartController private constructor(
  private val generalGameSettings: GeneralGameSettings,
  playersWithSettings: Map<UUID, JsonObject>,
  playersSpreadByTeams: Set<Set<UUID>>,
  private val gameFilters: JsonObject,
) {

  private val logger = createLogger()

  private val playersWithSettings: Map<UUID, JsonObject> = HashMap(playersWithSettings)
  private val playersSpreadByTeams: Set<Set<UUID>> = HashSet(playersSpreadByTeams)

  private val confirmedPlayers: MutableList<UUID> = ArrayList()
  private var roundCancelled = false
  private val playersStartConfirmationTime: MutableMap<UUID, Long> = HashMap()

  private fun startRound() {
    playersWithSettings.keys.forEach(PlayerMiniGameManager::removeFromSearchQueue)

    val newRound =
      createNewRound(
        generalGameSettings.name,
        playersWithSettings.keys,
        playersSpreadByTeams,
        null,
      )
    addProfile(newRound, "rounds")

    val roundPlayerSettings: MutableMap<Player, JsonObject> = HashMap()
    playersWithSettings.forEach { (key: UUID, value: JsonObject) ->
      roundPlayerSettings[getPlayerByUuid(key)!!] = value
    }

    val roundStartEvent =
      RoundPreStartEvent(
        roundPlayerSettings,
        playersSpreadByTeams,
        newRound,
        generalGameSettings.name,
        gameFilters,
      )
    EventDispatcher.call(roundStartEvent)
  }

  private fun startConfirmation() {
    for (playerUuid in playersWithSettings.keys) {
      showConfirmationGui(
        getPlayerByUuid(playerUuid)!!,
        generalGameSettings,
        onPlayerAccepted = onPlayerClickAccept,
        onPlayerDeclined = onPlayerClickDecline,
        onPlayerClosedInventory = this::onPlayerCloseConfirmation,
      )

      playersStartConfirmationTime[playerUuid] = System.currentTimeMillis()
    }

    MinecraftServer.getSchedulerManager()
      .buildTask { this.onConfirmationTimeOut() }
      .delay(TaskSchedule.millis(ROUND_CONFIRMATION_TIMEOUT))
      .schedule()
  }

  private val onPlayerClickAccept =
    GuiItem.PlayerInteractionHandler { _, player, _, clickType ->
      if (clickType.isNotLR) {
        return@PlayerInteractionHandler true
      }

      player.closeInventory()

      if (isConfirmationUnavailable(player.uuid)) {
        return@PlayerInteractionHandler true
      }

      confirmedPlayers.add(player.uuid)

      if (confirmedPlayers.size == playersWithSettings.size && !roundCancelled) {
        startRound()
      }
      return@PlayerInteractionHandler true
    }

  private val onPlayerClickDecline =
    GuiItem.PlayerInteractionHandler { _, player, _, clickType ->
      if (clickType.isNotLR) {
        return@PlayerInteractionHandler true
      }

      player.closeInventory()

      onPlayerDeclinedGame(player)
      return@PlayerInteractionHandler true
    }

  private fun onPlayerCloseConfirmation(player: Player) {
    if (isConfirmationUnavailable(player.uuid)) {
      return
    }
    if (confirmedPlayers.contains(player.uuid) || roundCancelled) {
      return
    }

    showConfirmationGui(
      player,
      generalGameSettings,
      onPlayerAccepted = onPlayerClickAccept,
      onPlayerDeclined = onPlayerClickDecline,
      onPlayerClosedInventory = this::onPlayerCloseConfirmation,
    )
  }

  private fun onConfirmationTimeOut() {
    if (confirmedPlayers.size == playersWithSettings.size || roundCancelled) {
      return
    }

    for (playerUuid in playersWithSettings.keys) {
      if (confirmedPlayers.contains(playerUuid)) {
        continue
      }

      val player = getPlayerByUuid(playerUuid)
      player!!.closeInventory()

      PlayerLanguage.get(player).sendMiniMessage("rounds.new-round.queue.fail.accept-timeout")

      onPlayerDeclinedGame(player)
    }
  }

  private fun onPlayerDeclinedGame(player: Player?) {
    if (roundCancelled) {
      removeFromSearchQueueAndUnlockGroup(player!!.uuid)
      return
    }

    roundCancelled = true

    playersWithSettings
      .keys
      .forEach { uuid: UUID ->
        getPlayerByUuid(uuid)?.closeInventory()
      }

    // Return players who confirmed game which did`n started to search
    for (confirmedPlayer in confirmedPlayers) {
      val queueElement = getMiniGameQueueElement(confirmedPlayer)
      if (queueElement != null) {
        queueElement.setStartingGame(false)
        queueElement.updateAddedToQueueTime()
      }
    }

    // Send message to player group
    val miniGameQueueElement =
      getMiniGameQueueElement(player!!.uuid)
    if (miniGameQueueElement == null) {
      logger.debug("MiniGameQueue element is Null for round declined: ${player.username}")
      return
    }

    for (memberUuid in miniGameQueueElement.playersWithSettings.keys) {
      if (memberUuid == player.uuid) {
        continue
      }

      PlayerLanguage.get(getPlayerByUuid(memberUuid)!!)
        .sendMiniMessage("group.round.declined", "player" to player.username)
    }

    removeFromSearchQueueAndUnlockGroup(player.uuid)
  }

  private fun isConfirmationUnavailable(playerUuid: UUID): Boolean = (
    (System.currentTimeMillis() - playersStartConfirmationTime.getOrDefault(playerUuid, 0L))
      > ROUND_CONFIRMATION_TIMEOUT
    )

  companion object {
    private val ROUND_CONFIRMATION_TIMEOUT = mainConfig.roundInvitationTimeout

    /**
     * Starting round without confirmation GUI (Debug)
     *
     * @param generalGameSettings General game settings
     * @param playersWithSettings Map of players with their settings
     * @param playersSpreadByTeams Sets of players teams
     * @param gameFilters Final game filters
     */
    fun startNewRoundWithoutConfirmation(
      generalGameSettings: GeneralGameSettings,
      playersWithSettings: Map<UUID, JsonObject>,
      playersSpreadByTeams: Set<Set<UUID>>,
      gameFilters: JsonObject,
    ) {
      val miniGameStartController =
        MiniGameStartController(
          generalGameSettings,
          playersWithSettings,
          playersSpreadByTeams,
          gameFilters,
        )

      miniGameStartController.startRound()
    }

    /**
     * Starting round with confirmation GUI
     *
     * @param generalGameSettings General game settings
     * @param playersWithSettings Map of players with their settings
     * @param playersSpreadByTeams Sets of players teams
     * @param gameFilters Final game filters
     */
    fun startNewRound(
      generalGameSettings: GeneralGameSettings,
      playersWithSettings: Map<UUID, JsonObject>,
      playersSpreadByTeams: Set<Set<UUID>>,
      gameFilters: JsonObject,
    ) {
      val miniGameStartController =
        MiniGameStartController(
          generalGameSettings,
          playersWithSettings,
          playersSpreadByTeams,
          gameFilters,
        )

      miniGameStartController.startConfirmation()
    }
  }
}
