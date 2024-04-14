package com.daylifecraft.minigames.text.i18n

import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.logging.foundation.LogEvent
import com.daylifecraft.minigames.util.FilesUtil.getResourceStreamByPath
import com.daylifecraft.minigames.util.FilesUtil.walkThrowResourcesDir
import net.kyori.adventure.text.Component
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.Properties

object Lang {
  const val LANGUAGE_DIR: String = "i18n"
  const val DEFAULT_LANGUAGE: String = "en_us"
  const val DEBUG_KEY: String = "debug.debug-string-welcome"
  private val translates = HashMap<String, HashMap<String, String>>()

  private val LOGGER = createLogger()

  @Throws(IOException::class)
  fun init() {
    // Path: i18n/<language_code>.properties
    translates.clear()
    LOGGER.debug("Loading languages")
    for (langFilePath in walkThrowResourcesDir(LANGUAGE_DIR)) {
      val langFile = langFilePath.fileName.toString()

      val langKey = langFile.replace(".properties", "").lowercase()
      val appProps = Properties()
      appProps.load(
        InputStreamReader(
          getResourceStreamByPath(Paths.get(LANGUAGE_DIR, langFile).toString()),
          StandardCharsets.UTF_8,
        ),
      )

      LOGGER.debug(
        "Loading language file: %s (name: %s). Keys: %s"
          .format(langFile, langKey, appProps.size),
      )

      val langDict = HashMap<String, String>()

      for (key in appProps.keys) {
        val keyStr = key.toString()
        langDict[keyStr] = appProps.getProperty(keyStr)
      }

      translates[langKey] = langDict
    }
    LOGGER.debug("Languages loaded success!")
  }

  @JvmStatic
  fun string(
    lang: String,
    key: String,
    vararg variables: Pair<String, String?>,
  ): String {
    val dict =
      if (translates.containsKey(lang) &&
        translates[lang]!!
          .containsKey(key)
      ) {
        translates[lang]!!
      } else if (translates.containsKey(DEFAULT_LANGUAGE) &&
        translates[DEFAULT_LANGUAGE]!!
          .containsKey(key)
      ) {
        translates[DEFAULT_LANGUAGE]!!
      } else {
        LOGGER.build(LogEvent.TRANSLATES_UNKNOWN_KEYS) {
          message("Key not found in user language and in DEFAULT_LANGUAGE!")
          details("key", key)
          details("userLanguage", lang)
          details("defaultLanguage", DEFAULT_LANGUAGE)
          details("variables", variables)
        }

        return "[U.n.t.r.a.n.s.l.a.t.e.d $key]"
      }

    dict[key]?.let {
      // TODO This code actually repeated. Reuse it
      var result = it
      for ((name, value) in variables) {
        result = result.replace("$($name)", value ?: "null")
      }
      return result
    }
    LOGGER.build(LogEvent.TRANSLATES_UNKNOWN_KEYS) {
      message(
        "WTF: The key is not contained in the dictionary. This is an internal error" +
          " because the dict variable contains a dictionary containing the key," +
          " and if it is not, an exception is thrown earlier.",
      )
      details("key", key)
      details("userLanguage", lang)
      details("defaultLanguage", DEFAULT_LANGUAGE)
      details("variables", variables)
    }
    return "$lang: $key"
  }

  fun stringAsComponent(
    lang: String,
    key: String,
    vararg variables: Pair<String, String?>,
  ): Component = Component.text(string(lang, key, *variables))
}
