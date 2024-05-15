package com.daylifecraft.minigames.minigames.settings

import com.daylifecraft.common.config.load
import com.daylifecraft.common.config.providers.yamlProvidersOf
import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.minigames.config.MiniGameSettingsConfig
import com.daylifecraft.minigames.util.FilesUtil.getResourceStreamByPathCatching
import com.daylifecraft.minigames.util.FilesUtil.walkThrowResourcesDir
import net.minestom.server.item.Material
import java.io.IOException
import java.io.InputStream
import java.nio.file.Paths

/** Manager that provide interaction with basic MiniGames settings, which loaded from config.yml  */
class MiniGamesSettingManager {
  private val generalGameSettingsMap = mutableMapOf<String, GeneralGameSettings>()

  /**
   * Calls on startup to load all MiniGames config.yml
   *
   * @return total loaded MiniGames config count
   */
  fun onStartupLoad(): Int {
    var totalLoaded = 0
    try {
      for (miniGameFolderPath in walkThrowResourcesDir(GAMES_FOLDER)) {
        val configFileInputStream =
          getResourceStreamByPathCatching(
            Paths.get(GAMES_FOLDER, miniGameFolderPath.fileName.toString(), "config.yml")
              .toString(),
          )
        if (configFileInputStream == null) {
          LOGGER.debug("Cannot find config.yml in folder ${miniGameFolderPath.fileName}")
          continue
        }

        loadConfigurationFile(miniGameFolderPath.fileName.toString(), configFileInputStream)
        configFileInputStream.close()
        totalLoaded++
      }
    } catch (exception: IOException) {
      LOGGER.debug("Problem with loading mini games configs: $exception")
    } catch (exception: IllegalArgumentException) {
      LOGGER.debug("Problem with loading mini games configs: $exception")
    }

    return totalLoaded
  }

  private fun loadConfigurationFile(folderName: String, inputStream: InputStream) {
    val config = load<MiniGameSettingsConfig>(
      *yamlProvidersOf(inputStream),
    )

    val generalGameSettings = GeneralGameSettings(
      config.name,
      config.displayNameKey,
      config.descriptionKey,
      Material.fromNamespaceId(config.guiBlock.lowercase())!!,
      config.public,
      config.permission,
      playersCountRange = config.minPlayers..config.maxPlayers,
      groupSizeRange = config.minGroupSize..config.maxGroupSize,
      config.gameConfig,
    )

    generalGameSettingsMap[folderName] = generalGameSettings
  }

  fun getGeneralGameSettings(gameKey: String): GeneralGameSettings? = generalGameSettingsMap[gameKey]

  val allLoadedMiniGamesSettings: Collection<GeneralGameSettings>
    get() = generalGameSettingsMap.values

  val loadedMiniGamesCount: Int
    get() = generalGameSettingsMap.size

  val publicLoadedMiniGamesCount: Int
    get() = allLoadedMiniGamesSettings.count { it.isPublic }

  val loadedMiniGamesIds: Set<String>
    get() = generalGameSettingsMap.keys

  companion object {
    private const val GAMES_FOLDER = "games"

    private val LOGGER = createLogger<MiniGamesSettingManager>()
  }
}
