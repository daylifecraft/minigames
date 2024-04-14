package com.daylifecraft.common.logging.loggers

import com.daylifecraft.common.logging.foundation.Level
import com.daylifecraft.common.logging.foundation.LogEvent
import com.daylifecraft.common.logging.foundation.LogRecord
import org.slf4j.Marker
import org.slf4j.helpers.AbstractLogger
import java.util.function.Consumer

private typealias Slf4jLevel = org.slf4j.event.Level

/**
 * Slf4j logger implementation that uses some callbacks to filtering log records and perform logging.
 *
 * @param name name of the logger
 *
 * @property logsConsumer the end point where [LogRecord]s actually being logged.
 * There are some standard ones like
 * [JsonLogsConsumer][com.daylifecraft.common.logging.loggers.consumers.JsonLogsConsumer]
 * [PlainTextLogsConsumer][com.daylifecraft.common.logging.loggers.consumers.PlainTextLogsConsumer]
 *
 * @property levelAllowedPredicate decides whether log level is allowed or not.
 * All log records will pass this check if this property set to null.
 *
 * @property logRecordFilter decides whether logging of [LogRecord] will be performed or not.
 * All log records will pass this check if this property set to null.
 */
class CallbacksProxyLogger(
  name: String,
  private val logsConsumer: Consumer<LogRecord>,
  private val levelAllowedPredicate: ((Level) -> Boolean)? = null,
  private val logRecordFilter: ((LogRecord) -> Boolean)? = null,
) : AbstractLogger() {

  init {
    // Set AbstractLogger name. There is no other option to achieve this
    super.name = name
  }

  override fun handleNormalizedLoggingCall(
    level: Slf4jLevel,
    marker: Marker?,
    messagePattern: String,
    arguments: Array<out Any>?,
    throwable: Throwable?,
  ) {
    val logRecord =
      if (arguments?.size == 1 && arguments[0] is LogRecord) {
        arguments[0] as LogRecord
      } else {
        LogRecord(
          level = Level.fromSlf4j(level),
          loggerName = name,
          eventName = LogEvent.RAW_SLF4J_LOGS.eventName,
          playerUuid = null,
          eventMessage = arguments?.let { messagePattern.format(it) } ?: messagePattern,
          eventDetails = throwable?.let { mapOf("throwable" to it) },
        )
      }

    // Do not perform logging if not permitted by filter. If null skip this check
    if (logRecordFilter?.invoke(logRecord) == false) {
      return
    }

    logsConsumer.accept(logRecord)
  }

  override fun isTraceEnabled(): Boolean = isLevelAllowed(Level.TRACE)

  override fun isTraceEnabled(marker: Marker?): Boolean = isTraceEnabled

  override fun isDebugEnabled(): Boolean = isLevelAllowed(Level.DEBUG)

  override fun isDebugEnabled(marker: Marker?): Boolean = isDebugEnabled

  override fun isInfoEnabled(): Boolean = isLevelAllowed(Level.INFO)

  override fun isInfoEnabled(marker: Marker?): Boolean = isInfoEnabled

  override fun isWarnEnabled(): Boolean = isLevelAllowed(Level.WARN)

  override fun isWarnEnabled(marker: Marker?): Boolean = isWarnEnabled

  override fun isErrorEnabled(): Boolean = isLevelAllowed(Level.ERROR)

  override fun isErrorEnabled(marker: Marker?): Boolean = isErrorEnabled

  override fun getFullyQualifiedCallerName(): String = getName()

  private fun isLevelAllowed(level: Level): Boolean = levelAllowedPredicate?.invoke(level) ?: true
}
