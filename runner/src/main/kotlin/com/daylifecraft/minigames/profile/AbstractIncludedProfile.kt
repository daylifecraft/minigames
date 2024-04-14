package com.daylifecraft.minigames.profile

import org.bson.Document
import org.bson.types.ObjectId

abstract class AbstractIncludedProfile protected constructor(
  id: ObjectId,
  version: Long,
  players: String,
) : AbstractProfile(id, version, players) {
  protected abstract val includingProfile: AbstractProfile
  protected abstract val profileName: String

  override fun set(parameter: String, value: Any) {
    parameters[parameter] = value

    includingProfile.set(profileName, deserialize())
  }

  protected abstract fun deserialize(): Document
}
