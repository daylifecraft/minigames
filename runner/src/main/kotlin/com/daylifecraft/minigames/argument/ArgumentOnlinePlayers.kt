package com.daylifecraft.minigames.argument

import com.daylifecraft.common.finder.OnlinePlayerFinder
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.suggestion.OnlinePlayersSuggestion
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.profile.settings.OnlineStatus
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.exception.ArgumentSyntaxException

class ArgumentOnlinePlayers(id: String) : ArgumentPlayer(id) {
  /**
   * Gets argument suggestion
   *
   * @return command continuation (tab)
   */
  val suggestion: OnlinePlayersSuggestion
  private var excludeFriends: Boolean = false
  private var excludeOffline: Boolean = false

  init {
    excludeSamePlayer(true)

    suggestion = OnlinePlayersSuggestion()

    setSuggestionCallback(suggestion)
  }

  /**
   * If true, argument can't be friend
   *
   * @param value should the friend be excluded?
   */
  fun excludeFriends(value: Boolean) {
    excludeFriends = value
    suggestion.excludeFriends(value)
  }

  /**
   * If true, argument can't be offline player
   *
   * @param value should the offline player be excluded?
   */
  fun excludeOffline(value: Boolean) {
    excludeOffline = value
    suggestion.excludeOffline(value)
  }

  /**
   * Parse sender and input(username) into OnlinePlayerFinder object
   *
   * @param sender sender
   * @param input username
   */
  @Throws(ArgumentSyntaxException::class)
  override fun parse(sender: CommandSender, input: String): OnlinePlayerFinder {
    val finder = super.parse(sender, input)

    val isSame = finder.isSamePlayer
    val isNull = finder.isPlayerNull

    if (isSame || isNull) {
      return OnlinePlayerFinder(input, isSame)
    }

    val playerProfile =
      DatabaseManager.getPlayerProfile(
        "uuid",
        CommandsManager.getUuidFromSender(sender).toString(),
      )!!

    val player = finder.player!!

    val isTargetOffline =
      excludeOffline && OnlineStatus.valueOf(player) == OnlineStatus.OFFLINE
    val isFriend =
      excludeFriends && playerProfile.getFriends()!!.contains(player.uuid.toString())

    if (!isTargetOffline && !isFriend) {
      return OnlinePlayerFinder(player, input)
    }

    return OnlinePlayerFinder(input, false, isTargetOffline, isFriend)
  }
}
