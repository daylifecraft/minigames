package com.daylifecraft.minigames.minigames.profile

import com.daylifecraft.common.util.safeCastToArray
import com.daylifecraft.minigames.Init.uUID
import com.daylifecraft.minigames.minigames.profile.RoundStatus.Companion.getByStringRepresentation
import com.daylifecraft.minigames.profile.AbstractProfile
import com.daylifecraft.minigames.profile.AbstractProfileDetails
import com.daylifecraft.minigames.text.i18n.Language
import org.bson.Document
import org.bson.types.ObjectId
import java.sql.Timestamp
import java.time.Instant
import java.util.Date
import java.util.UUID

/** Round object from database  */
class RoundProfile private constructor(
  id: ObjectId,
  serverUuid: UUID?,
  miniGameId: String,
  startDate: Date,
  roundStatus: RoundStatus?,
  endDate: Date?,
  players: List<UUID>,
  roundTeamsProfiles: List<RoundTeamProfile>?,
  rating: Array<Document>?,
) : AbstractProfile(id, 1, COLLECTION_NAME) {
  @JvmField
  val miniGameId: String
  private val startDate: Date
  var roundStatus: RoundStatus?
    private set
  private var endDate: Date?
  private val players: List<UUID>
  private var roundTeamsProfiles: List<RoundTeamProfile>? = null
  private val rating: Array<Document>?

  init {
    parameters[SERVER_UUID_KEY] = serverUuid.toString()

    this.miniGameId = miniGameId
    parameters[MINI_GAME_ID_KEY] = miniGameId

    this.startDate = Timestamp(startDate.time)
    parameters[START_DATE_KEY] = startDate

    this.roundStatus = roundStatus
    parameters[ROUND_STATUS_KEY] = roundStatus!!.stringRepresentation

    this.endDate = if (endDate == null) null else Timestamp(endDate.time)
    parameters[END_DATE_KEY] = endDate

    this.players = ArrayList(players)
    parameters[PLAYERS_KEY] = players.map { it.toString() }

    if (roundTeamsProfiles != null) {
      this.roundTeamsProfiles = ArrayList(roundTeamsProfiles)
      parameters[TEAMS_KEY] = roundTeamsProfiles.map(RoundTeamProfile::deserialize)
    }

    this.rating = rating?.clone()
    parameters[RATING_KEY] = rating
  }

  override fun getDetails(language: Language): AbstractProfileDetails? = null

  /**
   * Set round team profiles
   *
   * @param roundTeamsProfiles List of round team profiles
   */
  fun setRoundTeamsProfiles(roundTeamsProfiles: List<RoundTeamProfile>) {
    this.roundTeamsProfiles = ArrayList(roundTeamsProfiles)

    parameters[TEAMS_KEY] = roundTeamsProfiles.map { obj: RoundTeamProfile -> obj.deserialize() }
  }

  /**
   * Set RoundStatus and update in database
   *
   * @param roundStatus
   */
  fun setRoundStatusAndUpdate(roundStatus: RoundStatus) {
    this.roundStatus = roundStatus
    set(ROUND_STATUS_KEY, roundStatus.stringRepresentation)
  }

  private fun setEndDateAndUpdate(timestamp: Timestamp?) {
    endDate = if (timestamp == null) null else Timestamp(timestamp.time)
    set(END_DATE_KEY, endDate!!)
  }

  fun endRound() {
    setRoundStatusAndUpdate(RoundStatus.ENDED)
    setEndDateAndUpdate(Timestamp.from(Instant.now()))
  }

  companion object {
    const val COLLECTION_NAME: String = "rounds"
    const val SERVER_UUID_KEY: String = "serverUuid"
    const val MINI_GAME_ID_KEY: String = "miniGameId"
    const val START_DATE_KEY: String = "startDate"
    const val ROUND_STATUS_KEY: String = "roundStatus"
    const val END_DATE_KEY: String = "endDate"
    const val PLAYERS_KEY: String = "players"
    const val TEAMS_KEY: String = "teams"
    const val RATING_KEY: String = "rating"

    /**
     * Create new round with default start values
     *
     * @param miniGameId MiniGame ID
     * @param players Players UUIDs collection
     * @param playersSpreadByTeams Players spread by teams
     * @param rating Rating documents
     * @return Created RoundProfile
     */
    @JvmStatic
    fun createNewRound(
      miniGameId: String,
      players: Collection<UUID>,
      playersSpreadByTeams: Set<Set<UUID>>,
      rating: Array<Document>?,
    ): RoundProfile {
      val roundProfile =
        RoundProfile(
          ObjectId(),
          uUID,
          miniGameId,
          Timestamp(System.currentTimeMillis()),
          RoundStatus.INITIALIZING,
          null,
          players.toList(),
          null,
          rating,
        )

      val teams: MutableList<RoundTeamProfile> = ArrayList()
      for (teamPlayers in playersSpreadByTeams) {
        teams.add(RoundTeamProfile.createRoundTeam(teamPlayers.toList(), roundProfile))
      }
      roundProfile.setRoundTeamsProfiles(teams)

      return roundProfile
    }

    /**
     * Deserialize RoundProfile from document
     *
     * @param document MongoDB document
     * @return RoundProfile object from document
     */
    @JvmStatic
    fun getRoundProfileFromDocument(document: Document): RoundProfile {
      val roundProfile =
        RoundProfile(
          document.getObjectId("_id"),
          UUID.fromString(document.getString(SERVER_UUID_KEY)),
          document.getString(MINI_GAME_ID_KEY),
          document.getDate(START_DATE_KEY),
          getByStringRepresentation(document.getString(ROUND_STATUS_KEY)),
          document.getDate(END_DATE_KEY),
          document.getList(PLAYERS_KEY, String::class.java).map { name: String? -> UUID.fromString(name) },
          null,
          document[RATING_KEY].safeCastToArray<Document>(),
        )

      roundProfile.setRoundTeamsProfiles(
        document[TEAMS_KEY].safeCastToArray<Document>()
          ?.map { doc: Document -> RoundTeamProfile.deserialize(doc, roundProfile) }!!,
      )

      return roundProfile
    }
  }
}
