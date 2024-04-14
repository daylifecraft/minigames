package com.daylifecraft.minigames.minigames.profile

import com.daylifecraft.minigames.profile.AbstractIncludedProfile
import com.daylifecraft.minigames.profile.AbstractProfile
import com.daylifecraft.minigames.profile.AbstractProfileDetails
import com.daylifecraft.minigames.text.i18n.Language
import org.bson.Document
import org.bson.types.ObjectId
import java.util.UUID

private const val PLAYERS_KEY = "players"
private const val PROFILE_KEY = "teams"

class RoundTeamProfile(
  id: ObjectId,
  players: List<UUID>,
  override val includingProfile: AbstractProfile,
) : AbstractIncludedProfile(id, 1, PLAYERS_KEY) {
  override val profileName: String
    get() = PROFILE_KEY

  init {
    parameters[PLAYERS_KEY] = players.map { it.toString() }
  }

  public override fun deserialize(): Document {
    val document = Document("_id", id)

    for (key in keys) {
      document.append(key, get(key))
    }

    return document
  }

  // TODO make it val
  override fun getDetails(language: Language): AbstractProfileDetails? = null

  companion object {
    /**
     * Creates RoundTeamProfile object with specified players
     *
     * @param players Collection of players in team
     * @param roundProfile Including profile
     * @return new database object
     */
    @JvmStatic
    fun createRoundTeam(players: List<UUID>, roundProfile: RoundProfile): RoundTeamProfile = RoundTeamProfile(ObjectId(), players, roundProfile)

    /**
     * Deserializing RoundTeamProfile from database document
     *
     * @param document team document
     * @param roundProfile url to parent profile
     * @return deserialized RoundTeamProfile
     */
    @JvmStatic
    fun deserialize(document: Document, roundProfile: RoundProfile): RoundTeamProfile = RoundTeamProfile(
      document.getObjectId("_id"),
      document.getList(PLAYERS_KEY, String::class.java).map(UUID::fromString),
      roundProfile,
    )
  }
}
