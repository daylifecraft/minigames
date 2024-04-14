package com.daylifecraft.minigames.command.suggestion

import com.daylifecraft.common.util.extensions.minestom.currentArgumentValue
import com.daylifecraft.minigames.command.CommandsManager.getUuidFromSender
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.profile.settings.OnlineStatus
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionCallback
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.Player
import net.minestom.server.utils.StringUtils

class OnlinePlayersSuggestion : SuggestionCallback {
  private var excludeFriends = false
  private var excludeNotFriends = false
  private var excludeOffline = false
  private var excludeDifferentInstances = false

  fun excludeFriends(value: Boolean) {
    excludeFriends = value
  }

  fun excludeOffline(value: Boolean) {
    excludeOffline = value
  }

  fun excludeNotFriends(excludeNotFriends: Boolean) {
    this.excludeNotFriends = excludeNotFriends
  }

  /**
   * Modifies players-suggestion based on their instances
   * @param excludeDifferentInstances - When true => suggestion will contain only players from sender instance
   * When false => all players suggested
   */
  fun excludeDifferentInstances(excludeDifferentInstances: Boolean) {
    this.excludeDifferentInstances = excludeDifferentInstances
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

    val onlinePlayers =
      MinecraftServer.getConnectionManager().onlinePlayers
    val playerProfile =
      DatabaseManager.getPlayerProfile(
        "uuid",
        getUuidFromSender(sender).toString(),
      )!!

    for (player in onlinePlayers) {
      val isFriend = playerProfile.getFriends()!!.contains(player.uuid.toString())

      val isExcludeFriend = excludeFriends && isFriend
      val isExcludeNotFriend = excludeNotFriends && !isFriend

      val isOffline = excludeOffline && OnlineStatus.valueOf(player) == OnlineStatus.OFFLINE

      val isExcludeDifferentInstance =
        excludeDifferentInstances && player.instance != ((sender as Player).instance)

      val isIncluded = !isExcludeFriend && !isOffline && !isExcludeNotFriend && !isExcludeDifferentInstance

      if (player != sender && isIncluded) {
        suggestion.addEntry(SuggestionEntry(player.username))
      }
    }
  }
}
