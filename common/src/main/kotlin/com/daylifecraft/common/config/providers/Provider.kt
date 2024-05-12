package com.daylifecraft.common.config.providers

import com.daylifecraft.common.config.ConfigPath

interface Provider {
  fun int(configPath: ConfigPath): Int?
  fun long(configPath: ConfigPath): Long?
  fun float(configPath: ConfigPath): Float?
  fun double(configPath: ConfigPath): Double?
  fun boolean(configPath: ConfigPath): Boolean?
  fun string(configPath: ConfigPath): String?
  fun listSize(configPath: ConfigPath): Int?
}
