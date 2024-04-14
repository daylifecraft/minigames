package com.daylifecraft.minigames.profile.settings

import com.daylifecraft.minigames.profile.AbstractProfile
import com.daylifecraft.minigames.profile.AbstractProfileDetails
import com.daylifecraft.minigames.text.i18n.Language

class SettingsDetails internal constructor(profile: AbstractProfile, language: Language) : AbstractProfileDetails(profile, language) {
  override val text: String
    get() = ""
}
