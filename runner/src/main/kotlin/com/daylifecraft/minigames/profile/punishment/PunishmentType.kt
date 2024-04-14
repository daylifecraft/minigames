package com.daylifecraft.minigames.profile.punishment

enum class PunishmentType(val typeName: String) {
  BAN("ban"),
  MUTE("mute"),
  ;

  companion object {
    fun get(name: String): PunishmentType? = when (name) {
      BAN.typeName -> BAN
      MUTE.typeName -> MUTE
      else -> null
    }
  }
}
