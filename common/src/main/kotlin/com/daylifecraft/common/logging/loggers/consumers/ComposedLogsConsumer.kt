package com.daylifecraft.common.logging.loggers.consumers

import com.daylifecraft.common.logging.foundation.LogRecord
import java.util.function.Consumer

class ComposedLogsConsumer(private vararg val consumers: Consumer<LogRecord>) : Consumer<LogRecord> {

  override fun accept(logRecord: LogRecord) {
    consumers.forEach { it.accept(logRecord) }
  }
}
