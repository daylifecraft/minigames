package com.daylifecraft.minigames.profile.player

import com.daylifecraft.minigames.Init.stopServerWithError
import com.daylifecraft.minigames.database.DatabaseManager.getPlayerProfile
import com.daylifecraft.minigames.profile.AbstractProfile
import com.daylifecraft.minigames.profile.AbstractProfileDetails
import com.daylifecraft.minigames.profile.settings.SettingsProfile
import com.daylifecraft.minigames.profile.settings.SettingsProfile.Companion.createSettings
import com.daylifecraft.minigames.profile.settings.SettingsProfile.Companion.serialize
import com.daylifecraft.minigames.text.i18n.Language
import net.minestom.server.entity.Player
import org.bson.Document
import org.bson.types.ObjectId
import java.util.UUID

class PlayerProfile(
  id: ObjectId,
  val uuid: String,
  username: String,
  settingsDocument: Document?,
  permissions: List<String>?,
  rating: Array<Document?>?,
  friends: List<String>?,
) : AbstractProfile(id, 1, "players") {
  /**
   * Player UserName
   */
  val username: String

  /**
   * Permissions list (Stores string groups from server.yml)
   */
  val permissions: MutableList<String>?

  /**
   * Player Settings profile
   */
  var settings: SettingsProfile? = null
    private set

  private val rating: Array<Document?>?
  private var friends: MutableList<String>?

  init {
    parameters["uuid"] = uuid

    this.username = username
    parameters["username"] = username

    this.permissions = permissions?.toMutableList()
    parameters["permissions"] = permissions ?: emptyList<Any>()

    this.rating = rating
    parameters["rating"] = rating

    this.friends = friends?.toMutableList()
    parameters["friends"] = friends ?: emptyList<Any>()

    if (settingsDocument != null) {
      settings = serialize(settingsDocument, this)
      parameters["settings"] = settingsDocument
    }
  }

  fun setSettings(profile: SettingsProfile) {
    settings = profile
    parameters["settings"] = profile.deserialize()
  }

  fun getFriends(): List<String>? = friends

  private fun setFriends(friends: MutableList<String>?) {
    this.friends = friends
    set("friends", friends!!)
  }

  override fun getDetails(language: Language): AbstractProfileDetails = PlayerProfileDetails(this, language)

  fun addFriend(friend: String): Boolean {
    if (friends!!.contains(friend)) {
      return false
    }

    friends!!.add(friend)
    setFriends(friends)

    return true
  }

  fun removeFriend(friend: String): Boolean {
    if (!friends!!.contains(friend)) {
      return false
    }

    friends!!.remove(friend)
    setFriends(friends)

    return true
  }

  /**
   * Add permission for player profile in database with auto-sync
   * @param permission Permission name
   */
  fun addPermission(permission: String) {
    this.permissions?.add(permission)
    set("permissions", permissions!!)
  }

  /**
   * Remove permission from player profile in database with auto-sync
   * @param permission Permission name
   */
  fun removePermission(permission: String) {
    this.permissions?.remove(permission)
    set("permissions", permissions!!)
  }

  companion object {
    fun createProfile(
      player: Player,
      permissions: List<String>?,
      rating: Array<Document?>?,
      friends: MutableList<String>?,
    ): PlayerProfile {
      val profile =
        PlayerProfile(
          ObjectId(),
          player.uuid.toString(),
          player.username,
          null,
          permissions,
          rating,
          friends,
        )

      profile.setSettings(createSettings(profile))

      return profile
    }

    fun getUsername(uuid: UUID): String? =
      getUsername(uuid.toString())

    fun getUsername(uuid: String): String? {
      try {
        val profile = getPlayerProfile("uuid", uuid) ?: return null

        if ("00000000-0000-0000-0000-000000000000" == profile.uuid) {
          return "server"
        }

        return profile.username
      } catch (e: IllegalArgumentException) {
        stopServerWithError(e)
      }
    }

    fun getUuid(username: String): String? {
      try {
        return getPlayerProfile("username", username)?.uuid
      } catch (e: IllegalArgumentException) {
        stopServerWithError(e)
      }
    }
  }
}
