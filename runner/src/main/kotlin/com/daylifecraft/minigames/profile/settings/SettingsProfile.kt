package com.daylifecraft.minigames.profile.settings

import com.daylifecraft.minigames.profile.AbstractIncludedProfile
import com.daylifecraft.minigames.profile.AbstractProfile
import com.daylifecraft.minigames.profile.AbstractProfileDetails
import com.daylifecraft.minigames.profile.player.PlayerProfile
import com.daylifecraft.minigames.text.i18n.Language
import org.bson.Document
import org.bson.types.ObjectId

class SettingsProfile private constructor(
  id: ObjectId,
  language: String,
  onlineStatus: String,
  hidePlayers: Boolean,
  hideGlobalChat: Boolean,
  disablePartyInvites: Boolean,
  disablePM: Boolean,
  override val includingProfile: AbstractProfile,
) : AbstractIncludedProfile(id, 1, "players") {
  override val profileName: String
    get() = "settings"

  var language: String = language
    set(value) {
      set(LANGUAGE_KEY, value)
      field = value
    }
  var onlineStatus: String = onlineStatus
    set(value) {
      set(ONLINE_STATUS_KEY, value)
      field = value
    }
  var hidePlayers: Boolean = hidePlayers
    set(value) {
      set(HIDE_PLAYERS_KEY, value)
      field = value
    }
  var hideGlobalChat: Boolean = hideGlobalChat
    set(value) {
      set(HIDE_GLOBAL_CHAT_KEY, value)
      field = value
    }
  var disablePartyInvites: Boolean = disablePartyInvites
    set(value) {
      set(DISABLE_PARTY_INVITES_KEY, value)
      field = value
    }
  var disablePM: Boolean = disablePM
    set(value) {
      set(DISABLE_PM_KEY, value)
      field = value
    }

  init {
    parameters[LANGUAGE_KEY] = language
    parameters[HIDE_PLAYERS_KEY] = hidePlayers
    parameters[HIDE_GLOBAL_CHAT_KEY] = hideGlobalChat
    parameters[DISABLE_PARTY_INVITES_KEY] = disablePartyInvites
    parameters[DISABLE_PM_KEY] = disablePM
    parameters[ONLINE_STATUS_KEY] = onlineStatus
  }

  public override fun deserialize(): Document {
    // Get all keys from profile
    val keys = keys

    val document = Document("_id", id)

    for (key in keys) {
      // Add key and value to document
      document.append(key, get(key))
    }

    return document
  }

  override fun getDetails(language: Language): AbstractProfileDetails = SettingsDetails(this, language)

  companion object {
    const val LANGUAGE_KEY_AUTO: String = "auto"

    private const val LANGUAGE_KEY = "language"
    private const val ONLINE_STATUS_KEY = "onlineStatus"
    private const val HIDE_PLAYERS_KEY = "hidePlayers"
    private const val HIDE_GLOBAL_CHAT_KEY = "hideGlobalChat"
    private const val DISABLE_PARTY_INVITES_KEY = "disablePartyInvites"
    private const val DISABLE_PM_KEY = "disablePM"

    @JvmStatic
    fun createSettings(profile: PlayerProfile): SettingsProfile = SettingsProfile(
      id = ObjectId(),
      language = LANGUAGE_KEY_AUTO,
      onlineStatus = "online",
      hidePlayers = false,
      hideGlobalChat = false,
      disablePartyInvites = false,
      disablePM = false,
      includingProfile = profile,
    )

    // TODO Review nullability here. Probably some fields should become nullable
    @JvmStatic
    fun serialize(document: Document, profile: PlayerProfile): SettingsProfile = SettingsProfile(
      document["_id"] as ObjectId,
      document.getString(LANGUAGE_KEY)!!,
      document.getString(ONLINE_STATUS_KEY)!!,
      document.getBoolean(HIDE_PLAYERS_KEY)!!,
      document.getBoolean(HIDE_GLOBAL_CHAT_KEY)!!,
      document.getBoolean(DISABLE_PARTY_INVITES_KEY)!!,
      document.getBoolean(DISABLE_PM_KEY)!!,
      profile,
    )
  }
}
