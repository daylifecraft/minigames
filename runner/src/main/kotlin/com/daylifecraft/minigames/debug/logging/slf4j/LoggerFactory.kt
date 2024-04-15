package com.daylifecraft.minigames.debug.logging.slf4j

import com.daylifecraft.common.logging.loggers.CallbacksProxyLogger
import com.daylifecraft.common.logging.loggers.consumers.ComposedLogsConsumer
import com.daylifecraft.common.logging.loggers.consumers.JsonLogsConsumer
import com.daylifecraft.common.logging.loggers.consumers.PlainTextLogsConsumer
import com.daylifecraft.minigames.Dev
import com.daylifecraft.minigames.ServerUuidProvider
import com.daylifecraft.minigames.debug.logging.consumers.StackTraceToPlayerLogConsumer
import org.slf4j.ILoggerFactory
import org.slf4j.Logger

internal class LoggerFactory : ILoggerFactory {

  override fun getLogger(name: String): Logger {
    val logsConsumer = if (Dev.LOGGER_TEXT_LOGS) {
      ComposedLogsConsumer(
        StackTraceToPlayerLogConsumer(),
        PlainTextLogsConsumer(),
      )
    } else {
      JsonLogsConsumer(
        defaultProperties = mapOf("serverUuid" to ServerUuidProvider.uuid.toString()),
      )
    }

    return CallbacksProxyLogger(
      name = name,
      logsConsumer = logsConsumer,
      levelAllowedPredicate = { it.isProdAllowed || Dev.LOGGER_IS_SHOW_ALL_LOGS },
      logRecordFilter = if (Dev.LOGGER_FILTERING) Dev.LOGGER_FILTER else null,
    )
  }
}
