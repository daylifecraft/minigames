package com.daylifecraft.common.config

import com.daylifecraft.common.util.safeCastToList
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.io.InputStream
import java.util.Objects

class ConfigFile {
  private val yamlSetting =
    LoadSettings.builder().setLabel("Custom user configuration").build()

  private val yaml: Load
  private val values: MutableMap<String, Any?>

  constructor(inputStream: InputStream) {
    yaml = Load(yamlSetting)

    val iterable = yaml.loadAllFromInputStream(inputStream)
    this.values = convertFromIterable(iterable)
  }

  constructor(values: Map<String, Any>) {
    yaml = Load(yamlSetting)
    this.values = HashMap(values)
  }

  fun getValueFromList(
    key: String,
    filterKey: String,
    filterObject: Any,
  ): Map<String, Any?> {
    // Get values list
    val valueList: List<Map<String, Any?>> = getValueList(key)

    for (value in valueList) {
      if (value[filterKey] == filterObject) {
        return value
      }
    }

    return emptyMap()
  }

  fun getValueList(key: String): List<Map<String, Any?>> = (get(key) as List<*>).map { listElement ->
    (listElement as Map<*, *>).mapKeys { it.key as String }
  }

  fun getConfigSection(key: String): ConfigFile = ConfigFile(getValuesMap(key))

  // TODO remove when everything will be in kotlin and use getTypedList?
  fun getStringList(key: String): List<String>? =
    getTypedList(key)

  inline fun <reified T> getTypedList(key: String): List<T>? = get(key).safeCastToList()

  fun getString(key: String): String? =
    get(key) as String?

  fun getInt(key: String): Int? =
    get(key) as Int?

  fun getLong(key: String): Long? =
    get(key) as Long?

  fun getBool(key: String): Boolean? =
    get(key) as Boolean?

  fun getFloat(key: String): Float? =
    get(key) as Float?

  fun getDouble(key: String): Double? =
    get(key) as Double?

  fun get(key: String): Any? = getValues(key, false)[getLastToken(key)]

  fun getValuesMap(key: String): Map<String, Any> {
    if (get(key) == null) {
      return emptyMap()
    }

    val resultMap: MutableMap<String, Any> = HashMap()
    for ((key1, value) in (get(key) as Map<*, *>)) {
      resultMap[key1 as String] = value!!
    }

    return resultMap
  }

  fun getKeys(root: String?): List<String> {
    val keys: MutableList<String> = ArrayList()

    for (key in getValues(root!!, true).keys) {
      keys.add(key)
    }

    return keys
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other == null || javaClass != other.javaClass) {
      return false
    }
    val that = other as ConfigFile
    return values == that.values
  }

  override fun hashCode(): Int = Objects.hash(values)

  private fun getValues(path: String, isKey: Boolean): HashMap<String, Any?> {
    // Split path into keys
    val keys = path.split('.')

    // Return default key if path does not have any children
    if (keys.size <= 1) {
      return HashMap(values)
    }

    var rootValues = HashMap<String, Any?>()
    var lastRootValues: Map<String, Any?> = values

    // Apply keys
    for (i in 0 until keys.size - (if (isKey) 0 else 1)) {
      rootValues = LinkedHashMap()
      for ((key, value) in (lastRootValues[keys[i]] as LinkedHashMap<*, *>?)!!) {
        rootValues[key as String] = value
      }
      lastRootValues = rootValues
    }

    return rootValues
  }

  private fun convertFromIterable(iterable: Iterable<Any>): MutableMap<String, Any?> {
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

  private fun getLastToken(key: String): String =
    key.split('.').last()

  fun setValue(key: String, newObject: Any) {
    values[key] = newObject
  }
}
