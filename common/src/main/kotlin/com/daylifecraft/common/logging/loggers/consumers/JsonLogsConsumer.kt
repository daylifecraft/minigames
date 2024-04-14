package com.daylifecraft.common.logging.loggers.consumers

import com.daylifecraft.common.logging.foundation.LogRecord
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.function.Consumer

class JsonLogsConsumer(private val defaultProperties: Map<String, Any?>? = null) : Consumer<LogRecord> {

  override fun accept(logRecord: LogRecord) {
    val resultJson = createJsonWithDefaultProperties()

    resultJson.addProperty("level", logRecord.level.key)
    resultJson.addProperty("loggerName", logRecord.loggerName)
    resultJson.addProperty("timestamp", logRecord.timestamp)
    resultJson.addProperty("eventUuid", logRecord.eventUuid.toString())
    if (logRecord.playerUuid != null) {
      resultJson.addProperty("playerUuid", logRecord.playerUuid.toString())
    }
    resultJson.addProperty("eventName", logRecord.eventName)
    resultJson.add("event", logRecord.eventToJson())

    println(resultJson)
  }

  private fun createJsonWithDefaultProperties(): JsonObject = JsonObject().also { result ->
    defaultProperties?.let { result.putMap(it) }
  }
}

private fun LogRecord.eventToJson(): JsonElement {
  val event = JsonObject()
  event.addProperty("message", eventMessage)

  eventDetails?.let { event.putMap(it) }

  return event
}

private fun JsonObject.tryAddStackTrace(key: String, value: Array<*>) {
  if (value.isArrayOf<StackTraceElement>()) {
    val jsonStackTrace = JsonArray()
    value.forEach { jsonStackTrace.add(it.toString()) }
    add(key, jsonStackTrace)
  }
}

private fun JsonObject.putMap(map: Map<String, *>) {
  for ((key, value) in map) {
    when (value) {
      is String -> addProperty(key, value)

      is Number -> addProperty(key, value)

      is Boolean -> addProperty(key, value)

      is Array<*> -> tryAddStackTrace(key, value)

      is Map<*, *> -> {
        add(
          key,
          JsonObject().apply {
            val hashMap = HashMap<String, Any?>()
            value.forEach { (k, v) -> hashMap[k as String] = v }

            putMap(hashMap)
          },
        )
      }

      else -> addProperty(key, value.toString())
    }
  }
}
