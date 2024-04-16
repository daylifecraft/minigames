package com.daylifecraft.minigames.logging

import com.daylifecraft.common.logging.foundation.Level
import com.daylifecraft.common.logging.foundation.LogRecord
import com.daylifecraft.common.logging.loggers.CallbacksProxyLogger
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class CallbacksProxyLoggerTest {
  @ParameterizedTest
  @EnumSource(Level::class)
  fun testWithoutFiltering(level: Level) {
    val logRecord = LogRecord(
      level = level,
      loggerName = "tests logger",
      eventName = "test event",
    )

    var wasCalled = false
    val logger = CallbacksProxyLogger(
      "tests logger",
      logsConsumer = {
        wasCalled = true
      },
    )

    logger.makeLoggingEventBuilder(level.toSlf4j()).log("{}", logRecord)

    assert(wasCalled)
  }

  @ParameterizedTest
  @MethodSource("logLevelFilteringArgs")
  fun testFilteringByLevel(actualLevel: Level, allowedLevel: Level, expectation: Boolean) {
    val logRecord = LogRecord(
      level = actualLevel,
      loggerName = "tests logger",
      eventName = "test event",
    )

    var wasCalled = false
    val logger = CallbacksProxyLogger(
      "tests logger",
      logsConsumer = {
        wasCalled = true
      },
      levelAllowedPredicate = {
        it == allowedLevel
      },
    )

    logger.makeLoggingEventBuilder(actualLevel.toSlf4j()).log("{}", logRecord)

    val expectationMessage = if (expectation) "should" else "should not"
    assertEquals(expectation, wasCalled, "Logger $expectationMessage be called")
  }

  @ParameterizedTest
  @MethodSource("logRecordsFilteringArgs")
  fun testFilteringByRecord(loggerName: String, expectation: Boolean) {
    val logRecord = LogRecord(
      level = Level.DEBUG,
      loggerName = loggerName,
      eventName = "test event",
    )

    var wasCalled = false
    val logger = CallbacksProxyLogger(
      "tests logger",
      logsConsumer = {
        wasCalled = true
      },
      logRecordFilter = {
        BANNED_LOGGER_NAME !in it.loggerName
      },
    )

    logger.debug("{}", logRecord)

    val expectationMessage = if (expectation) "should" else "should not"
    assertEquals(expectation, wasCalled, "Logger $loggerName $expectationMessage be called")
  }

  companion object {
    private const val BANNED_LOGGER_NAME = "banned logger"

    @JvmStatic
    fun logLevelFilteringArgs() = listOf(
      Arguments.of(Level.TRACE, Level.DEBUG, false),
      Arguments.of(Level.DEBUG, Level.INFO, false),
      Arguments.of(Level.INFO, Level.WARN, false),
      Arguments.of(Level.WARN, Level.ERROR, false),
      Arguments.of(Level.ERROR, Level.TRACE, false),

      Arguments.of(Level.TRACE, Level.TRACE, true),
      Arguments.of(Level.DEBUG, Level.DEBUG, true),
      Arguments.of(Level.INFO, Level.INFO, true),
      Arguments.of(Level.WARN, Level.WARN, true),
      Arguments.of(Level.ERROR, Level.ERROR, true),
    )

    @JvmStatic
    fun logRecordsFilteringArgs() = listOf(
      Arguments.of("LoggerA", true),
      Arguments.of("LoggerB", true),

      Arguments.of("LoggerA$BANNED_LOGGER_NAME", false),
      Arguments.of("LoggerB$BANNED_LOGGER_NAME", false),
    )
  }
}
