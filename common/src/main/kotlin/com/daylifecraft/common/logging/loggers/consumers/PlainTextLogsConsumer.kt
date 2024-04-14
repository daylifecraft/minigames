package com.daylifecraft.common.logging.loggers.consumers

import com.daylifecraft.common.logging.foundation.LogRecord
import java.util.function.Consumer

class PlainTextLogsConsumer : Consumer<LogRecord> {

  override fun accept(logRecord: LogRecord) {
    val suffixBuilder = StringBuilder("\n")

    logRecord.eventDetails?.forEach { (key, value) ->
      when (value) {
        is Array<*> -> suffixBuilder.tryAddStackTrace(key, value)
        is Throwable -> suffixBuilder.addThrowableTo(key, value)
        else -> suffixBuilder.addKeyValue(key, value)
      }
    }

    System.out.printf(
      "[%s/%s] [%s] [%s] %s%s",
      logRecord.level.key.uppercase(),
      logRecord.thread.name,
      logRecord.eventName,
      logRecord.loggerName,
      logRecord.eventMessage,
      suffixBuilder,
    )
  }
}

private fun StringBuilder.addKeyValue(key: String, value: Any?) {
  this.append(" * ").append(key).append(" = ").append(value).append("\n")
}

private fun StringBuilder.addThrowableTo(key: String, value: Throwable) {
  this.append(" * ").append(key).append(": ").append(value).append("\n")
  this.append(" * StackTrace of Throwable:").append(key).append(":\n")
  for (stackTraceElement in value.stackTrace) {
    this.append(" * - at ").append(stackTraceElement.toString()).append("\n")
  }
}

private fun StringBuilder.tryAddStackTrace(key: String, value: Array<*>) {
  if (!value.isArrayOf<StackTraceElement>()) return

  this.append(" * ").append(key).append(":\n")
  for (stackTraceElement in value) {
    this.append(" * - at ").append(stackTraceElement).append("\n")
  }
}
