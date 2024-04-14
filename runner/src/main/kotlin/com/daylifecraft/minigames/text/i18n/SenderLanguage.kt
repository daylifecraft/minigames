package com.daylifecraft.minigames.text.i18n

import com.daylifecraft.common.util.extensions.miniMessage
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender

class SenderLanguage private constructor(val sender: CommandSender) : Language {
  override fun string(key: String, vararg variables: Pair<String, String?>): String = Lang.string(lang, key, *variables)

  override fun stringAsComponent(key: String, vararg variables: Pair<String, String?>): Component = Lang.stringAsComponent(lang, key, *variables)

  override fun sendString(key: String, vararg variables: Pair<String, String?>) {
    sender.sendMessage(string(key, *variables))
  }

  override fun miniMessage(key: String, vararg variables: Pair<String, String?>): Component = string(key, *variables).miniMessage()

  override fun sendMiniMessage(key: String, vararg variables: Pair<String, String?>) {
    sender.sendMessage(miniMessage(key, *variables))
  }

  val lang: String = Lang.DEFAULT_LANGUAGE

  companion object {
    @JvmStatic
    fun get(player: CommandSender): SenderLanguage = SenderLanguage(player)
  }
}
