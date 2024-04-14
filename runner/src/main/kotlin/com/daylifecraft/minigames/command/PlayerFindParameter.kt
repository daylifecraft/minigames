package com.daylifecraft.minigames.command

import java.util.regex.Pattern

enum class PlayerFindParameter {
  UUID,
  USERNAME,
  EMPTY,
  ;

  companion object {
    private val USERNAME_PATTERN: Pattern = Pattern.compile("^([A-Za-z0-9_]){3,16}$")
    private val UUID_PATTERN: Pattern =
      Pattern.compile(
        "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$",
      )

    fun get(playerString: String): PlayerFindParameter = if (USERNAME_PATTERN.matcher(playerString).matches()) { // Username
      USERNAME
    } else if (UUID_PATTERN.matcher(playerString).matches()) { // UUID
      UUID
    } else { // Empty
      EMPTY
    }
  }
}
