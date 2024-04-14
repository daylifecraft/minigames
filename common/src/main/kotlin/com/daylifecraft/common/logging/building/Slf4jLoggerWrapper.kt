package com.daylifecraft.common.logging.building

import com.daylifecraft.common.logging.foundation.LogEvent

typealias Slf4jLogger = org.slf4j.Logger

internal class Slf4jLoggerWrapper(
  val wrappedLogger: Slf4jLogger,
) : Logger {
  override fun build(event: LogEvent, builder: LogBuilder.() -> Unit) {
    val providedBuilder = LogBuilderImpl(this, event)
    builder.invoke(providedBuilder)
    providedBuilder.complete()
  }
}
