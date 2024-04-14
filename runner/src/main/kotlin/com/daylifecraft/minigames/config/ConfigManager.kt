package com.daylifecraft.minigames.config

import com.daylifecraft.common.config.ConfigFile
import com.daylifecraft.minigames.util.FilesUtil

object ConfigManager {
  /**
   * Gets main config file
   *
   * @return server.yml ConfigFile
   */
  @JvmStatic
  var mainConfig: ConfigFile? = null // TODO lateinit it
    private set

  /** Load configs  */
  @JvmStatic
  fun load() {
    mainConfig = ConfigFile(FilesUtil.getResourceStreamByPath("server.yml"))
  }
}
