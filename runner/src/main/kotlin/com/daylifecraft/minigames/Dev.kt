package com.daylifecraft.minigames

import com.daylifecraft.common.logging.foundation.LogRecord
import com.daylifecraft.minigames.Init.serverEnv

// TODO split it by responsibility (isDev -> Init, logger filtering -> log util)
object Dev {
  /** Default logs filter  */
  val LOGGER_FILTER: (LogRecord) -> Boolean = this::removeDataBaseLogsPredicate

  // Show logs in text format
  val LOGGER_TEXT_LOGS: Boolean = isDev

  // enable this if you need show Level.DEBUG logs
  val LOGGER_IS_SHOW_ALL_LOGS: Boolean = isDev

  // show player debug strings in chat
  val LOGGER_PLAYER_MINECRAFT_CHAT: Boolean = isDev

  // is use LOGGER_FILTER
  val LOGGER_FILTERING: Boolean = isDev

  @JvmStatic
  var isDev: Boolean
    get() = devStatus ?: "DEV".equals(serverEnv, ignoreCase = true).also { devStatus = it }
    set(value) {
      devStatus = value
    }

  private var devStatus: Boolean? = null

  private fun removeDataBaseLogsPredicate(logRecord: LogRecord): Boolean {
    // remove db logs
    return !logRecord.loggerName.contains("mongodb")
  }
}
