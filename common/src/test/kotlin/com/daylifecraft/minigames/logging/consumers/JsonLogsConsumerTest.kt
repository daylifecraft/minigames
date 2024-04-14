package com.daylifecraft.minigames.logging.consumers

import com.daylifecraft.common.logging.foundation.Level
import com.daylifecraft.common.logging.foundation.LogRecord
import com.daylifecraft.common.logging.loggers.consumers.JsonLogsConsumer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.util.UUID
import kotlin.test.assertContains

class JsonLogsConsumerTest {
  @ParameterizedTest
  @MethodSource("params")
  fun testLogsContainsAllData(record: LogRecord) {
    logsConsumer.accept(record)

    val printed = outStream.toString(Charset.defaultCharset())

    assertContains(printed, record.level.key)
    assertContains(printed, record.loggerName)
    assertContains(printed, record.eventName)
    record.playerUuid?.let { assertContains(printed, it.toString()) }
    record.eventMessage?.let { assertContains(printed, it) }
    record.eventDetails?.let {
      assertContains(printed, it.keys.first())
      assertContains(printed, it.values.first().toString())
    }
  }

  @ParameterizedTest
  @MethodSource("params")
  fun testDefaultPropsAreLogged(record: LogRecord) {
    val defaultProperties = mapOf(
      "prop1" to "val1",
      "prop2" to "val2",
    )
    JsonLogsConsumer(defaultProperties).accept(record)

    val printed = outStream.toString(Charset.defaultCharset())

    for ((key, value) in defaultProperties) {
      assertContains(printed, key)
      assertContains(printed, value)
    }
  }

  companion object {
    private val logsConsumer = JsonLogsConsumer()

    private val outStream = ByteArrayOutputStream()
    private val original = System.out

    @JvmStatic
    fun params() = listOf(
      LogRecord(
        level = Level.DEBUG,
        loggerName = "test logger",
        eventName = "test event",
        playerUuid = UUID.randomUUID(),
        eventMessage = "event message",
        eventDetails = mapOf("detail" to 42),
      ),
      LogRecord(
        level = Level.DEBUG,
        loggerName = "test logger",
        eventName = "test event",
        playerUuid = UUID.randomUUID(),
        eventMessage = "event message",
      ),
      LogRecord(
        level = Level.DEBUG,
        loggerName = "test logger",
        eventName = "test event",
        playerUuid = UUID.randomUUID(),
      ),
      LogRecord(
        level = Level.DEBUG,
        loggerName = "test logger",
        eventName = "test event",
      ),
    )

    @BeforeAll
    @JvmStatic
    fun setStreams() {
      System.setOut(PrintStream(outStream))
    }

    @AfterAll
    @JvmStatic
    fun restoreInitialStreams() {
      System.setOut(original)
    }
  }
}
