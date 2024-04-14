package com.daylifecraft.minigames.profile

import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.profile.player.PlayerProfile
import com.daylifecraft.minigames.profile.settings.OnlineStatus
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.util.UUID

object GuiControl {
  /**
   * Set the player's online-mode in its settings
   *
   * @param player player instance
   * @param status status to set
   */
  @JvmStatic
  fun setSettingsOnlineMode(player: Player, status: OnlineStatus) {
    DatabaseManager.getPlayerProfile(player)!!.settings!!.onlineStatus = status.dbKey
  }

  /**
   * Get the player's current online-mode
   *
   * @param player player instance
   */
  @JvmStatic
  fun getSettingsOnlineStatus(player: Player): OnlineStatus = OnlineStatus.ofDBKey(
    DatabaseManager.getPlayerProfile(player)!!.settings!!.onlineStatus,
  )

  /**
   * Checks if the player's private messages are disabled
   *
   * @param player player instance
   */
  @JvmStatic
  fun isDisablePrivateMessages(player: Player): Boolean = DatabaseManager.getPlayerProfile(player)!!.settings!!.disablePM

  /**
   * Set the status of the player's private messages
   *
   * @param player player instance
   * @param b status
   */
  @JvmStatic
  fun setDisablePrivateMessages(player: Player, b: Boolean) {
    DatabaseManager.getPlayerProfile(player)!!.settings!!.disablePM = b
  }

  /**
   * Checks whether party invitations are disabled
   *
   * @param player player instance
   */
  @JvmStatic
  fun isDisablePartyInvites(player: Player): Boolean = DatabaseManager.getPlayerProfile(player)!!.settings!!.disablePartyInvites

  /**
   * Sets whether party invitations are disabled
   *
   * @param player player instance
   * @param b status
   */
  @JvmStatic
  fun setDisablePartyInvites(player: Player, b: Boolean) {
    DatabaseManager.getPlayerProfile(player)!!.settings!!.disablePartyInvites = b
  }

  /**
   * Checks whether global chat is hidden
   *
   * @param player player instance
   */
  @JvmStatic
  fun isHideGlobalChat(player: Player): Boolean = DatabaseManager.getPlayerProfile(player)!!.settings!!.hideGlobalChat

  /**
   * Set whether global chat is hidden
   *
   * @param player player instance
   * @param b status
   */
  @JvmStatic
  fun setHideGlobalChat(player: Player, b: Boolean) {
    DatabaseManager.getPlayerProfile(player)!!.settings!!.hideGlobalChat = b
  }

  /**
   * Checks if players are hidden
   *
   * @param player player instance
   */
  @JvmStatic
  fun isHidePlayers(player: Player): Boolean = DatabaseManager.getPlayerProfile(player)!!.settings!!.hidePlayers

  /**
   * Sets whether players are hidden
   *
   * @param player player instance
   * @param b status
   */
  @JvmStatic
  fun setHidePlayers(player: Player, b: Boolean) {
    DatabaseManager.getPlayerProfile(player)!!.settings!!.hidePlayers = b
    PlayerManager.setPlayerHide(player, b)
  }

  /**
   * Gets the number of player friends
   *
   * @param player player instance
   */
  @JvmStatic
  fun getFriendsCount(player: Player): Int = DatabaseManager.getPlayerProfile(player)!!.getFriends()!!.size

  /**
   * Gets the uuid of player friends
   *
   * @param player player instance
   */
  @JvmStatic
  fun getFriendsStringUuids(player: Player): Array<String> = DatabaseManager.getPlayerProfile(player)!!.getFriends()!!.toTypedArray<String>()

  /**
   * Gets player profile by uuid
   *
   * @param friendUuid player uuid
   */
  @JvmStatic
  fun getPlayerProfileByStringUuid(friendUuid: String): PlayerProfile? = DatabaseManager.getPlayerProfile("uuid", friendUuid)

  // TODO 08.09.2023 currently check only current instance
  @JvmStatic
  fun isPlayerOnlineInServer(profile: PlayerProfile): Boolean = (
    MinecraftServer.getConnectionManager()
      .getOnlinePlayerByUuid(UUID.fromString(profile.uuid))
      != null
    )
}
