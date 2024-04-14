package com.daylifecraft.minigames.text.i18n

import com.daylifecraft.common.util.extensions.miniMessage
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.profile.player.PlayerProfile
import com.daylifecraft.minigames.profile.settings.SettingsProfile
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player

class PlayerLanguage private constructor(val player: Player) : Language {
  private val profile: PlayerProfile = PlayerManager.loadPlayer(player)

  override fun string(key: String, vararg variables: Pair<String, String?>): String = Lang.string(lang, key, *variables)

  override fun stringAsComponent(key: String, vararg variables: Pair<String, String?>): Component = Lang.stringAsComponent(lang, key, *variables)

  override fun sendString(key: String, vararg variables: Pair<String, String?>) {
    player.sendMessage(string(key, *variables))
  }

  override fun miniMessage(key: String, vararg variables: Pair<String, String?>): Component = string(key, *variables).miniMessage()

  override fun sendMiniMessage(key: String, vararg variables: Pair<String, String?>) {
    player.sendMessage(miniMessage(key, *variables))
  }

  val lang: String
    get() {
      // TODO refactor but save logic
      var language: String? = profile.settings!!.language
      if (language.equals(LANGUAGE_KEY_AUTO, ignoreCase = true)) {
        language = player.settings.locale
        if (language == null) {
          language = Lang.DEFAULT_LANGUAGE
        }
      }
      return language!!.lowercase()
    }

  companion object {
    private const val LANGUAGE_KEY_AUTO = SettingsProfile.LANGUAGE_KEY_AUTO

    @JvmStatic
    fun get(player: Player): PlayerLanguage = PlayerLanguage(player)
  }
}
