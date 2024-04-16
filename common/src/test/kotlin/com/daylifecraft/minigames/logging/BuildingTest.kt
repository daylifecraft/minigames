package com.daylifecraft.minigames.logging

import com.daylifecraft.common.logging.building.Slf4jLoggerWrapper
import com.daylifecraft.common.logging.foundation.LogEvent
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.spi.LoggingEventBuilder
import java.util.function.Supplier

class BuildingTest {

  private val mockedBuilder = mockk<LoggingEventBuilder>()
  private val mockedLogger = mockk<Logger>()

  @BeforeEach
  fun setupMocks() {
    every { mockedBuilder.setMessage(any<String>()) } returns mockedBuilder
    every { mockedBuilder.addArgument(any<Supplier<*>>()) } returns mockedBuilder
    justRun { mockedBuilder.log() }

    every { mockedLogger.makeLoggingEventBuilder(any()) } returns mockedBuilder
  }

  @Test
  fun testLogPerformedAfterBuilding() {
    Slf4jLoggerWrapper(mockedLogger).build(LogEvent.GENERAL_DEBUG) {
      message("message")
    }

    verify(exactly = 1) { mockedBuilder.log() }
  }

  @Test
  fun testCorrectFormatBeingUsed() {
    Slf4jLoggerWrapper(mockedLogger).build(LogEvent.GENERAL_DEBUG) {
      message("message")
    }

    verify(exactly = 1) { mockedBuilder.setMessage("{}") }
  }
}
