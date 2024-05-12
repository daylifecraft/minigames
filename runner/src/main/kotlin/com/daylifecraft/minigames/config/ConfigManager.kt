package com.daylifecraft.minigames.config

import com.daylifecraft.common.config.load
import com.daylifecraft.common.config.providers.YamlProvider
import com.daylifecraft.minigames.util.FilesUtil

object ConfigManager {
  /**
   * Gets main config file
   *
   * @return server.yml ConfigFile
   */
  lateinit var mainConfig: MainConfig
    private set

  /** Load configs  */
  fun load() {
    mainConfig = load(
      YamlProvider(FilesUtil.getResourceStreamByPath("server.yml")),
    )
  }
}
