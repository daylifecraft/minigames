package com.daylifecraft.common.variable

import com.daylifecraft.common.exception.EnvironmentVariableNullException
import java.io.File
import java.io.IOException

/**
 * Setting variable
 * @property name of the variable
 * @param defaultValue value that will be returned by default if no other will be loaded
 * @property isRequired if set to true [load] will throw exception on null
 */
class Variable(
  val name: String,
  defaultValue: String? = null,
  val isRequired: Boolean = false,
) {

  var value: String? = defaultValue
    private set

  /**
   * Loads variable.
   *
   * Tries to load variable from file by path from [name]_FILE env variable.
   * If that env not present, then tries to load from [name] env variable.
   *
   * @throws [EnvironmentVariableNullException] if variable [isRequired] but not found.
   */
  fun load() {
    val tempValue = loadValueFromFile() ?: getEnv(name)

    if (tempValue != null) {
      value = tempValue
    } else if (isRequired && value == null) {
      throw EnvironmentVariableNullException(this)
    }
  }

  private fun loadValueFromFile(): String? {
    val filePath = getEnv("${name}_FILE")
    try {
      return filePath?.let { path ->
        File(path).readText().trim()
      }
    } catch (e: Exception) {
      throw IOException("Cannot read variable \"$name\" from file: $filePath", e)
    }
  }

  private fun getEnv(name: String): String? =
    System.getenv(name)?.trim()
}
