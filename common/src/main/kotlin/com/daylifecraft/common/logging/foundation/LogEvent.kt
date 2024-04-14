package com.daylifecraft.common.logging.foundation

/**
 * @property eventName name of the event which will be displayed in logs.
 * @property defaultLevel default log level of event type.
 * Used to determine default log level of [LogRecord]
 */
enum class LogEvent(val eventName: String, val defaultLevel: Level) {
  GENERAL_DEBUG("generalDebug", Level.DEBUG),

  MONGODB("mongodb", Level.INFO),

  SERVER_CRASHED("server_crashed", Level.ERROR),
  SERVER_STARTED("server_started", Level.DEBUG),

  PLAYER_JOINED("player_joined", Level.INFO),
  PLAYER_JOIN_FAIL("player_join_fail", Level.WARN),

  TRANSLATES_UNKNOWN_KEYS("translates_unknown_keys", Level.ERROR),

  RAW_SLF4J_LOGS("raw_slf4j_logs", Level.INFO),

  PROFILER("profiler", Level.DEBUG),
}
