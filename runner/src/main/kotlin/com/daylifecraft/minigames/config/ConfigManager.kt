package com.daylifecraft.minigames.config

import com.daylifecraft.common.config.ConfigFile
import com.daylifecraft.minigames.util.FilesUtil

object ConfigManager {
  /**
   * Gets main config file
   *
   * @return server.yml ConfigFile
   */
  lateinit var mainConfig: ConfigFile
    private set

  /** Load configs  */
  fun load() {
    mainConfig = ConfigFile(FilesUtil.getResourceStreamByPath("server.yml"))
  }
}
