package com.daylifecraft.minigames

import com.daylifecraft.common.logging.building.LogBuilder
import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.logging.foundation.LogEvent
import com.daylifecraft.common.util.NetworkUtil.getPlayerAddress
import com.daylifecraft.common.util.NetworkUtil.getPlayerIp
import com.daylifecraft.common.util.TimeUtil.currentUtcSeconds
import com.daylifecraft.common.util.extensions.miniMessage
import com.daylifecraft.minigames.command.CommandsManager.getSenderLanguage
import com.daylifecraft.minigames.database.DatabaseManager.addProfile
import com.daylifecraft.minigames.database.DatabaseManager.getPlayerProfile
import com.daylifecraft.minigames.event.player.LoadPlayerEvent
import com.daylifecraft.minigames.exception.InvalidPlayerUsername
import com.daylifecraft.minigames.profile.player.PlayerProfile
import com.daylifecraft.minigames.profile.punishment.PunishmentType
import com.daylifecraft.minigames.profile.punishment.PunishmentsManager
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import net.minestom.server.permission.Permission
import net.minestom.server.utils.mojang.MojangUtils
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.regex.Pattern

// TODO must be split at least into two responsibilities (friends and other)

/** Player manager uses for managing players (show bans)  */
object PlayerManager {
  private val username_PATTERN: Pattern = Pattern.compile("^([A-Za-z0-9_]){3,16}$")
  private val LOGGER = createLogger()
  private const val FRIENDS_KEY = "friends."

  /**
   * kick player from server
   *
   * @param player player to be kick
   * @param error error that player will receive
   */
  fun kickPlayerError(player: Player, error: String) {
    // TODO use lang key for error. (ex: Server internal error:\n\n$(internalErrorDetails))
    player.kick(error.miniMessage())
  }

  /**
   * kick player from server
   *
   * @param player player to be kick
   * @param reason reason that player will receive
   */
  fun kickPlayer(player: Player, reason: String) {
    player.kick(MiniMessage.miniMessage().deserialize(reason))
  }

  /**
   * used to get a PlayerProfile object for a specific player
   *
   * @param player the player whose profile we need to get
   * @return PlayerProfile from player
   */
  fun loadPlayer(player: Player): PlayerProfile {
    // Applying player to the database

    var playerProfile: PlayerProfile?

    // Get player profile by uuid
    playerProfile = getPlayerProfile(player)

    if (playerProfile == null) {
      // Create new profile if not exist
      addProfile(PlayerProfile.createProfile(player, null, null, null), "players")

      // Get player profile by uuid
      playerProfile = getPlayerProfile("uuid", player.uuid.toString())
    }

    val loadPlayerEvent = LoadPlayerEvent(player, playerProfile!!)
    EventDispatcher.call(loadPlayerEvent)

    return playerProfile
  }

  /**
   * used to get a list of permissions for a specific player
   *
   * @param player the player whose permissions we need to get
   * @return list of permissions(strings)
   */
  fun getStringPermissions(player: Player): List<String> {
    // Get all player`s permissions
    val permissions = player.allPermissions

    val stringPermissions: MutableList<String> = ArrayList()

    for (permission in permissions) {
      // Convert permission to string
      stringPermissions.add(permission.permissionName)
    }

    return stringPermissions
  }

  /**
   * used to add a specific player to a group
   *
   * @param player the player we add to the group
   * @param group ID of the group to which the player will be added
   */
  fun applyGroup(player: Player, group: String) {
    player.addPermission(Permission("group.$group"))
  }

  /**
   * used to add certain permissions to the player
   *
   * @param player the player we need to add permissions
   * @param permissions permissions the player will receive
   */
  fun applyPermissions(player: Player, permissions: List<String?>?) {
    if (permissions.isNullOrEmpty()) {
      return
    }

    for (permission in permissions) {
      player.addPermission(Permission(permission!!))
    }
  }

  /**
   * Checks if the player has active punishments with @punishmentType
   *
   * @param player Player instance
   * @param punishmentType Punishment type
   * @return true if player have active punishment, else returns false
   */
  fun checkPunishments(player: Player, punishmentType: PunishmentType, doPreventActions: Boolean): Boolean {
    // Get all player`s punishments
    val profiles = PunishmentsManager.getList(player.uuid)

    val currentTime = currentUtcSeconds()
    val totalEndTime = PunishmentsManager.getTotalEndTime(player.uuid, punishmentType)

    if (profiles.isEmpty() || (totalEndTime != -1L && totalEndTime <= currentTime)) {
      return false
    }

    if (!doPreventActions) {
      return true
    }

    PunishmentsManager.getPunishmentManager(punishmentType).show(player, totalEndTime)

    return true
  }

  /**
   * Returns the player to normal state (as when entering the server)
   *
   * @param player Player instance
   */
  fun resetPlayer(player: Player) {
    player.closeInventory()
    player.clearTitle()
  }

  /**
   * Gets player UserName by UUID or returns default value, if player == null (does not found)
   *
   * @param uuid Player uuid
   * @param defaultValue Default username
   * @return Username
   */
  fun getPlayerNameByUuidOrDefault(uuid: UUID, defaultValue: String): String {
    val player = getPlayerByUuid(uuid) ?: return defaultValue

    return player.name.toString()
  }

  /**
   * Searches for a player with a specific uuid
   *
   * @param uuid Player uuid
   * @return player with that uuid
   */
  fun getPlayerByUuid(uuid: UUID): Player? = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid)

  /**
   * Searches for a player with a specific username
   *
   * @param username Player username
   * @return player with that username
   */
  fun getPlayerByUserName(username: String): Player? =
    getPlayerByUuid(getPlayerUuid(username))

  /**
   * Add a new friend for a player
   *
   * @param sender player who want to add new friend
   * @param playerUuid player sender uuid
   * @param friend friend's instance to add
   */
  fun addFriend(
    sender: CommandSender,
    playerUuid: UUID,
    friend: Player?,
  ) {
    setFriend(sender, playerUuid, friend, FriendAction.ADD)
  }

  /**
   * Remove an old friend for a player
   *
   * @param sender player who want to remove new friend
   * @param playerUuid player sender uuid
   * @param friend friend's instance to remove
   */
  fun removeFriend(
    sender: CommandSender,
    playerUuid: UUID,
    friend: Player?,
  ) {
    setFriend(sender, playerUuid, friend, FriendAction.REMOVE)
  }

  /**
   * Add a new friend for a player
   *
   * @param sender player who want to add new friend
   * @param playerUuid player sender uuid
   * @param friendUsername friend's username to add
   */
  fun addFriend(
    sender: CommandSender,
    playerUuid: UUID,
    friendUsername: String,
  ) {
    setFriend(sender, playerUuid, friendUsername, FriendAction.ADD)
  }

  /**
   * Remove an old friend for a player
   *
   * @param sender player who want to remove new friend
   * @param playerUuid player sender uuid
   * @param friendUsername friend's username to remove
   */
  fun removeFriend(
    sender: CommandSender,
    playerUuid: UUID,
    friendUsername: String,
  ) {
    setFriend(sender, playerUuid, friendUsername, FriendAction.REMOVE)
  }

  /**
   * Add a new friend for a player
   *
   * @param sender player who want to add new friend
   * @param playerUuid player sender uuid
   * @param friendUuid friend's uuid to add
   */
  fun addFriend(
    sender: CommandSender,
    playerUuid: UUID,
    friendUuid: UUID?,
  ) {
    setFriend(sender, playerUuid, friendUuid, FriendAction.ADD)
  }

  /**
   * Remove an old friend for a player
   *
   * @param sender player who want to remove new friend
   * @param playerUuid player sender uuid
   * @param friendUuid friend's uuid to remove
   */
  fun removeFriend(
    sender: CommandSender,
    playerUuid: UUID,
    friendUuid: UUID?,
  ) {
    setFriend(sender, playerUuid, friendUuid, FriendAction.REMOVE)
  }

  /**
   * Gets the uuid of a player by username
   *
   * @param username player username
   * @return uuid for player
   */
  fun getPlayerUuid(username: String): UUID {
    if (!username_PATTERN.matcher(username).matches()) {
      throw InvalidPlayerUsername(username)
    }
    if (!Init.onlineMode) {
      return UUID.nameUUIDFromBytes(username.toByteArray(StandardCharsets.UTF_8))
    }

    return MojangUtils.getUUID(username)
  }

  private fun setFriend(
    sender: CommandSender,
    playerUuid: UUID,
    friend: Player?,
    action: FriendAction,
  ) {
    // Get uuid from friend

    var uuid = UUID(0, 0)

    if (friend != null) {
      uuid = friend.uuid
    }
    setFriend(sender, playerUuid, uuid, action)
  }

  private fun setFriend(
    sender: CommandSender,
    playerUuid: UUID,
    friendUsername: String,
    action: FriendAction,
  ) {
    setFriend(sender, playerUuid, getPlayerUuid(friendUsername), action)
  }

  private fun setFriend(
    sender: CommandSender,
    playerUuid: UUID,
    friendUuid: UUID?,
    action: FriendAction,
  ) {
    if (sender !is Player) {
      return
    }

    // Get sender language
    val language = getSenderLanguage(sender)

    // Get friend profile
    val friendProfile = getPlayerProfile("uuid", friendUuid.toString())

    if (friendProfile == null) {
      sender.sendMessage(language.miniMessage(action.failMessageKey))
      return
    }

    // Get player profile
    val playerProfile = getPlayerProfile("uuid", playerUuid.toString())!!

    val actionPerformed =
      when (action) {
        FriendAction.ADD -> playerProfile.addFriend(friendProfile.uuid)
        FriendAction.REMOVE -> playerProfile.removeFriend(friendProfile.uuid)
      }

    // Add friend to player
    if (actionPerformed) {
      sender.sendMessage(
        language.miniMessage(
          action.successMessageKey,
          "targetPlayer" to friendProfile.username,
        ),
      )
    } else {
      sender.sendMessage(language.miniMessage(action.failMessageKey))
    }
  }

  /**
   * Set visibility of other players for a specific player
   *
   * @param player player's instance
   * @param value boolean variable for setting the view
   */
  fun setPlayerHide(player: Entity, value: Boolean) {
    player.setAutoViewEntities(!value)
  }

  fun logPlayerConnectError(
    player: Player,
    reason: String?,
    e: Exception,
  ) {
    val address = getPlayerAddress(player)
    val ip = getPlayerIp(address)
    val port = address.port.toLong()

    LOGGER.build(LogEvent.PLAYER_JOIN_FAIL) {
      player(player)
      message("Player cannot connect to the server")
      details("reason", reason)
      details("playerIp", ip)
      details("playerPort", port)
      detailsSection(LogBuilder.KEY_ADDITIONAL, "stackTrace", e.stackTrace)
      detailsSection(LogBuilder.KEY_ADDITIONAL, "throwable", e.message)
    }
  }

  private enum class FriendAction(private val lowerName: String) {
    ADD("add"),
    REMOVE("remove"),
    ;

    val successMessageKey
      get() = "$FRIENDS_KEY$lowerName"
    val failMessageKey
      get() = "$FRIENDS_KEY$lowerName-fail"
  }
}
