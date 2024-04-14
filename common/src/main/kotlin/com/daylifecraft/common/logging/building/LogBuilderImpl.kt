package com.daylifecraft.common.logging.building

import com.daylifecraft.common.logging.foundation.Level
import com.daylifecraft.common.logging.foundation.LogEvent
import com.daylifecraft.common.logging.foundation.LogRecord
import java.util.UUID

internal class LogBuilderImpl(private val logger: Slf4jLoggerWrapper, private val event: LogEvent) : LogBuilder {

  private val details = mutableMapOf<String, Any?>()
  private var level = event.defaultLevel
  private var player: UUID? = null
  private var message: String? = null

  override fun player(player: UUID) {
    this.player = player
  }

  override fun level(level: Level) {
    this.level = level
  }

  override fun message(s: String?) {
    message = s
  }

  override fun details(key: String, value: Any?) {
    details[key] = value
  }

  override fun detailsSection(section: String, key: String, value: Any?) {
    val map = mutableMapOf<String, Any?>()
    val detail = details.computeIfAbsent(section) {
      mutableMapOf<String, Any?>()
    }

    if (detail is Map<*, *>) {
      detail.mapKeysTo(map) { it.key as String }
    } else {
      logger.wrappedLogger.debug("[LOGGER] [BUILDER] invalid current details section.")
      return
    }

    map[key] = value
  }

  override fun complete() {
    logger.wrappedLogger.makeLoggingEventBuilder(level.toSlf4j()).setMessage("{}").addArgument {
      LogRecord(
        level = level,
        loggerName = logger.wrappedLogger.name,
        eventName = event.eventName,
        playerUuid = player,
        eventMessage = message,
        eventDetails = details.takeUnless { it.isEmpty() },
      )
    }.log()
  }
}
