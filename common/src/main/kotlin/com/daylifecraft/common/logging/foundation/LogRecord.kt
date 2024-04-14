package com.daylifecraft.common.logging.foundation

import java.time.Instant
import java.util.UUID

/**
 * @property level Log record level. Used to filter logs
 * @property loggerName name of logger on which [LogRecord] was created
 * @property eventName name of event. Usually defined in [LogEvent]
 * @property eventUuid uuid of event. Random by default
 * @property timestamp UTC time in milliseconds when LogRecord was created.
 * @property thread thread on which logging was called
 * @property playerUuid uuid of player which can be added by specific events like [LogEvent.PLAYER_JOIN_FAIL]
 * @property eventMessage log message. This property is optional but usually it is set.
 * @property eventDetails map with additional data of any type
 * (even other maps called details sections) associated with this event.
 */
data class LogRecord(
  val level: Level,
  val loggerName: String,
  val eventName: String,
  val eventUuid: UUID = UUID.randomUUID(),
  val timestamp: Long = Instant.now().toEpochMilli(),
  val thread: Thread = Thread.currentThread(),
  val playerUuid: UUID? = null,
  val eventMessage: String? = null,
  val eventDetails: Map<String, Any?>? = null,
) {
  override fun toString(): String = """

      level = $level,
      loggerName = $loggerName,
      eventName = $eventName,
      eventUuid = $eventUuid,
      timestamp = $timestamp,
      thread = $thread,
      playerUuid = $playerUuid,
      eventMessage = $eventMessage,
      eventDetails = $eventDetails,
  """.trimIndent().prependIndent("   ")
}
