package com.daylifecraft.minigames.text.i18n

import net.kyori.adventure.text.Component

interface Language {
  fun string(key: String, vararg variables: Pair<String, String?>): String

  fun stringAsComponent(key: String, vararg variables: Pair<String, String?>): Component

  fun sendString(key: String, vararg variables: Pair<String, String?>)

  fun miniMessage(key: String, vararg variables: Pair<String, String?>): Component

  fun sendMiniMessage(key: String, vararg variables: Pair<String, String?>)
}
