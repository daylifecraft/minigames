package com.daylifecraft.minigames.argument

import com.daylifecraft.common.finder.FriendPlayerFinder
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.suggestion.FriendsSuggestion
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.profile.player.PlayerProfile
import com.daylifecraft.minigames.profile.punishment.PunishmentType
import com.daylifecraft.minigames.profile.settings.OnlineStatus
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import java.util.UUID

class ArgumentFriends(id: String) : ArgumentPlayer(id) {
  /**
   * Gets argument suggestion
   *
   * @return command continuation (tab)
   */
  val suggestion: FriendsSuggestion

  private var excludeDisabledPmPlayers: Boolean
  private var excludeMutedPlayers: Boolean
  private var excludeOfflinePlayers: Boolean

  init {
    excludeSamePlayer(true)

    excludeDisabledPmPlayers = false
    excludeMutedPlayers = false
    excludeOfflinePlayers = false

    suggestion = FriendsSuggestion(this)

    // Set tab completer
    setSuggestionCallback(suggestion)
  }

  /**
   * If true, argument can't be player with disabled PM
   *
   * @param value should the player with disabled PM player be excluded?
   */
  fun excludeDisabledPmPlayers(value: Boolean) {
    excludeDisabledPmPlayers = value
    suggestion.excludeDisabledPmPlayers(value)
  }

  /**
   * If true, argument can't be muted player
   *
   * @param value should the muted player be excluded?
   */
  fun excludeMutedPlayers(value: Boolean) {
    excludeMutedPlayers = value
    suggestion.excludeMutedPlayers(value)
  }

  /**
   * If true, argument can't be offline player
   *
   * @param value should the offline player be excluded?
   */
  fun excludeOfflinePlayers(value: Boolean) {
    excludeOfflinePlayers = value
    suggestion.excludeOfflinePlayers(value)
  }

  @Throws(ArgumentSyntaxException::class)
  override fun parse(sender: CommandSender, input: String): FriendPlayerFinder = parse(sender, input, true)

  /**
   * Parse sender and input(username) into FriendPlayerFinder object
   *
   * @param sender sender
   * @param input username
   */
  @Throws(ArgumentSyntaxException::class)
  fun parse(
    sender: CommandSender,
    input: String,
    returnPlayer: Boolean,
  ): FriendPlayerFinder {
    val finder = super.parse(sender, input)

    val isSame = finder.isSamePlayer
    val isNull = finder.isPlayerNull

    if (isSame || isNull) {
      return FriendPlayerFinder(input, isSame)
    }

    val player = finder.player!!
    val settingsProfile = DatabaseManager.getPlayerProfile(player)!!.settings!!

    // Get player profile
    val profile =
      DatabaseManager.getPlayerProfile(
        "uuid",
        CommandsManager.getUuidFromSender(sender).toString(),
      )!!
    // Get friend uuid from input
    val uuid = PlayerProfile.getUuid(player.username)

    val isDisabledPM = settingsProfile.disablePM
    val isMuted = PlayerManager.checkPunishments(player, PunishmentType.MUTE, true)
    val isOffline = OnlineStatus.valueOf(player) == OnlineStatus.OFFLINE

    val excludeDisabledPM = excludeDisabledPmPlayers && isDisabledPM
    val excludeMuted = excludeMutedPlayers && isMuted
    val excludeOffline = excludeOfflinePlayers && isOffline
    val isNotFriend = !profile.getFriends()!!.contains(uuid)
    val isNotNull = uuid != null
    val isAllow = !isNotFriend && !excludeDisabledPM && !excludeMuted && !excludeOffline

    val foundPlayer =
      MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(UUID.fromString(uuid))

    // Check weather has player friend
    if (isNotNull && isAllow && returnPlayer && foundPlayer != null) {
      return FriendPlayerFinder(foundPlayer, input)
    }

    return FriendPlayerFinder(input, false, isNotFriend, isDisabledPM, isMuted, isOffline)
  }
}
