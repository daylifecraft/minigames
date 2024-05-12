package com.daylifecraft.common.config.providers

import com.daylifecraft.common.config.ConfigPath
import com.daylifecraft.common.config.IntToken
import com.daylifecraft.common.config.StringToken
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.io.InputStream

fun yamlProvidersOf(vararg yamlStreams: InputStream): Array<out Provider> =
  Array(yamlStreams.size) {
    YamlProvider(yamlStreams[it])
  }

class YamlProvider(yamlStream: InputStream) : Provider {
  private val yamlSetting = LoadSettings.builder().setLabel("Custom user configuration").build()

  private val data: Map<String, Any?> = Load(yamlSetting).loadTreeMapFromInputStream(yamlStream)

  private fun Load.loadTreeMapFromInputStream(yamlStream: InputStream): Map<String, Any?> {
    val iterable = loadAllFromInputStream(yamlStream)

    val result = mutableMapOf<String, Any?>()

    for (map in iterable.filterIsInstance<Map<*, *>>()) {
      map.forEach { (key, value) ->
        if (key is String) {
          result[key] = value
        }
      }
    }

    return result
  }

  override fun int(configPath: ConfigPath): Int? = when (val res = any(configPath, data)) {
    is Number -> res.toInt()
    else -> null
  }
  override fun long(configPath: ConfigPath): Long? = when (val res = any(configPath, data)) {
    is Number -> res.toLong()
    else -> null
  }

  override fun float(configPath: ConfigPath): Float? = when (val res = any(configPath, data)) {
    is Number -> res.toFloat()
    else -> null
  }

  override fun double(configPath: ConfigPath): Double? = when (val res = any(configPath, data)) {
    is Number -> res.toDouble()
    else -> null
  }

  override fun boolean(configPath: ConfigPath): Boolean? = any(configPath, data) as Boolean?

  override fun string(configPath: ConfigPath): String? = any(configPath, data) as String?

  override fun listSize(configPath: ConfigPath): Int? = (any(configPath, data) as List<*>?)?.size

  private fun any(path: ConfigPath, currentData: Map<String, Any?>): Any? {
    val pathToken = path.first()
    if (pathToken !is StringToken) error("path wasn't a StringToken $path")
    return moveDeeper(path, currentData[pathToken.value])
  }

  private fun any(path: ConfigPath, currentData: List<*>): Any? {
    val firstToken = path.first()
    if (firstToken !is IntToken) error("path wasn't an IntToken $path")
    return moveDeeper(path, currentData[firstToken.value])
  }

  @Suppress("UNCHECKED_CAST")
  private fun moveDeeper(path: ConfigPath, current: Any?): Any? {
    if (path.size == 1) return current
    if (current == null) return null

    val removed = path.removeFirst()
    val next = when (current) {
      is List<*> -> any(path, current)
      else -> any(path, current as Map<String, Any>)
    }
    path.addFirst(removed)
    return next
  }
}
