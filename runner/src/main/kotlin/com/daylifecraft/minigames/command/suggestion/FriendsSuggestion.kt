package com.daylifecraft.minigames.command.suggestion

import com.daylifecraft.common.util.extensions.minestom.currentArgumentValue
import com.daylifecraft.minigames.argument.ArgumentFriends
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.profile.player.PlayerProfile
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionCallback
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.utils.StringUtils

class FriendsSuggestion(private val argument: ArgumentFriends) : SuggestionCallback {
  private var excludeDisabledPmPlayers = false
  private var excludeMutedPlayers = false
  private var excludeOfflinePlayers = true
  private var excludeFriendsPlayers = false

  fun excludeDisabledPmPlayers(value: Boolean) {
    excludeDisabledPmPlayers = value
  }

  fun excludeMutedPlayers(value: Boolean) {
    excludeMutedPlayers = value
  }

  fun excludeOfflinePlayers(value: Boolean) {
    excludeOfflinePlayers = value
  }

  fun excludeFriendsPlayers(excludeFriendsPlayers: Boolean) {
    this.excludeFriendsPlayers = excludeFriendsPlayers
  }

  override fun apply(
    sender: CommandSender,
    context: CommandContext,
    suggestion: Suggestion,
  ) {
    val input = suggestion.currentArgumentValue

    if (input.contains(StringUtils.SPACE)) {
      return
    }

    // Get player profile
    val playerProfile =
      DatabaseManager.getPlayerProfile(
        "uuid",
        CommandsManager.getUuidFromSender(sender).toString(),
      )

    if (playerProfile == null) {
      return
    }

    // Add friends usernames to suggestions
    for (friendUuid in playerProfile.getFriends()!!) {
      val friendUsername = PlayerProfile.getUsername(friendUuid) ?: continue

      val finder = argument.parse(sender, friendUsername, false)

      val isDisabledPM = excludeDisabledPmPlayers && finder.isDisabledPm
      val isMuted = excludeMutedPlayers && finder.isMuted
      val isOffline = excludeOfflinePlayers && finder.isNotOnline
      val isFriend = excludeFriendsPlayers && !finder.isNotFriend

      if (!(isDisabledPM || isMuted || isOffline || isFriend)) {
        suggestion.addEntry(SuggestionEntry(friendUsername))
      }
    }
  }
}
