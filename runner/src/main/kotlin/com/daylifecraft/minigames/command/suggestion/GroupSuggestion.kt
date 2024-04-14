package com.daylifecraft.minigames.command.suggestion

import com.daylifecraft.common.util.extensions.minestom.addEntries
import com.daylifecraft.common.util.extensions.minestom.currentArgumentValue
import com.daylifecraft.common.util.extensions.minestom.toSuggestionEntry
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.profile.group.PlayersGroupManager
import com.daylifecraft.minigames.profile.player.PlayerProfile
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionCallback
import net.minestom.server.utils.StringUtils

class GroupSuggestion : SuggestionCallback {
  override fun apply(
    sender: CommandSender,
    context: CommandContext,
    suggestion: Suggestion,
  ) {
    val input = suggestion.currentArgumentValue

    if (input.contains(StringUtils.SPACE)) return

    val senderUUID = CommandsManager.getUuidFromSender(sender)
    val playersGroup = PlayersGroupManager.getGroupByPlayer(senderUUID) ?: return

    suggestion.addEntries(playersGroup.getAllPlayersUUIDs()) {
      if (it == senderUUID) return@addEntries null
      PlayerProfile.getUsername(it.toString()).toSuggestionEntry()
    }
  }
}
