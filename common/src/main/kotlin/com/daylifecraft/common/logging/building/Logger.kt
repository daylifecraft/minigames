package com.daylifecraft.common.logging.building

import com.daylifecraft.common.logging.foundation.LogEvent

interface Logger {

  /** Function to build log record and send it automatically when building is done.  */
  fun build(event: LogEvent, builder: LogBuilder.() -> Unit)

  /** Creates GENERAL_DEBUG log event with given message  */
  fun debug(message: String) = build(LogEvent.GENERAL_DEBUG) { message(message) }
}
