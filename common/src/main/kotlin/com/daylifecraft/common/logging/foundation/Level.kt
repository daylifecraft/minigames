package com.daylifecraft.common.logging.foundation

private typealias Slf4jLevel = org.slf4j.event.Level

/**
 * Log level. Mirrors slf4j Level and holds some additional data
 *
 * @property key name of level used in default loggers.
 * @property isProdAllowed determine will record with this level being logged on prod or not.
 */
enum class Level(val key: String, val isProdAllowed: Boolean) {
  // DEV
  TRACE("trace", false),
  DEBUG("debug", false),

  // PROD
  INFO("info", true),
  WARN("warn", true),
  ERROR("error", true),
  ;

  /** Returns corresponding Slf4j log level  */
  fun toSlf4j(): Slf4jLevel = when (this) {
    TRACE -> Slf4jLevel.TRACE
    DEBUG -> Slf4jLevel.DEBUG
    INFO -> Slf4jLevel.INFO
    WARN -> Slf4jLevel.WARN
    ERROR -> Slf4jLevel.ERROR
  }

  companion object {
    /** Returns log level for corresponding Slf4j level  */
    fun fromSlf4j(level: Slf4jLevel): Level = when (level) {
      Slf4jLevel.TRACE -> TRACE
      Slf4jLevel.DEBUG -> DEBUG
      Slf4jLevel.INFO -> INFO
      Slf4jLevel.WARN -> WARN
      Slf4jLevel.ERROR -> ERROR
    }
  }
}
