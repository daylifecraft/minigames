package com.daylifecraft.common.config.providers

import com.daylifecraft.common.config.ConfigPath
import com.daylifecraft.common.config.IntToken
import com.daylifecraft.common.config.StringToken
import java.util.Locale

class EnvProvider(
  private val prefix: String = "",
) : Provider {

  override fun int(configPath: ConfigPath): Int? = string(configPath)?.toInt()

  override fun long(configPath: ConfigPath): Long? = string(configPath)?.toLong()

  override fun float(configPath: ConfigPath): Float? = string(configPath)?.toFloat()

  override fun double(configPath: ConfigPath): Double? = string(configPath)?.toDouble()

  override fun boolean(configPath: ConfigPath): Boolean? = string(configPath)?.toBoolean()

  override fun string(configPath: ConfigPath): String? = System.getenv(configPath.toEnv())

  override fun listSize(configPath: ConfigPath): Int? = int(configPath)

  private fun ConfigPath.toEnv(): String = prefix + tokens.joinToString("_") {
    when (it) {
      is StringToken -> it.value
      is IntToken -> "[${it.value}]"
    }
  }.uppercase(Locale.getDefault())
}
