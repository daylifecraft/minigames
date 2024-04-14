package com.daylifecraft.minigames.argument

import com.daylifecraft.minigames.command.suggestion.GroupSuggestion
import net.minestom.server.command.builder.arguments.ArgumentString

class ArgumentGroupUsername(id: String) : ArgumentString(id) {
  init {
    setSuggestionCallback(GroupSuggestion())
  }
}
