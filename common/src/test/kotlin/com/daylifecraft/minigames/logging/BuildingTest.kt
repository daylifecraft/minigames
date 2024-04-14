package com.daylifecraft.minigames.logging

import com.daylifecraft.common.logging.building.Slf4jLoggerWrapper
import com.daylifecraft.common.logging.foundation.LogEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.slf4j.Logger
import org.slf4j.spi.LoggingEventBuilder
import java.util.function.Supplier

class BuildingTest {

  private val mockedBuilder: LoggingEventBuilder = mock()
  private val mockedLogger: Logger = mock()

  @BeforeEach
  fun setupMocks() {
    whenever(mockedBuilder.setMessage(any<String>())).thenReturn(mockedBuilder)
    whenever(mockedBuilder.addArgument(any<Supplier<*>>())).thenReturn(mockedBuilder)

    whenever(mockedLogger.makeLoggingEventBuilder(any())).thenReturn(mockedBuilder)
  }

  @Test
  fun testLogPerformedAfterBuilding() {
    Slf4jLoggerWrapper(mockedLogger).build(LogEvent.GENERAL_DEBUG) {
      message("message")
    }

    verify(mockedBuilder, times(1)).log()
  }

  @Test
  fun testCorrectFormatBeingUsed() {
    Slf4jLoggerWrapper(mockedLogger).build(LogEvent.GENERAL_DEBUG) {
      message("message")
    }

    verify(mockedBuilder, times(1)).setMessage("{}")
  }
}
