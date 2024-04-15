package com.daylifecraft.minigames.database

import com.daylifecraft.common.database.Database
import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.util.safeCastToArray
import com.daylifecraft.common.util.safeCastToList
import com.daylifecraft.common.variable.VariablesManager.getString
import com.daylifecraft.common.variable.VariablesRegistry
import com.daylifecraft.minigames.Init.stopServerWithError
import com.daylifecraft.minigames.config.ConfigManager.mainConfig
import com.daylifecraft.minigames.minigames.profile.RoundProfile
import com.daylifecraft.minigames.profile.AbstractProfile
import com.daylifecraft.minigames.profile.player.PlayerProfile
import com.daylifecraft.minigames.profile.punishment.PunishmentProfile
import com.daylifecraft.minigames.profile.settings.SettingsProfile.Companion.createSettings
import com.daylifecraft.minigames.profile.settings.SettingsProfile.Companion.serialize
import com.mongodb.ConnectionString
import com.mongodb.client.FindIterable
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import net.minestom.server.entity.Player
import org.bson.Document
import org.bson.types.ObjectId
import java.sql.Timestamp
import java.util.Date
import java.util.UUID

object DatabaseManager {
  private lateinit var database: Database
  private val LOGGER = createLogger()

  /** Setup connection and load database  */
  @JvmStatic
  fun load(
    dbName: String = getString(VariablesRegistry.SETTINGS_MONGODB_DATABASE)!!,
  ) {
    val connection =
      ConnectionString(
        "mongodb://" +
          getString(VariablesRegistry.SETTINGS_MONGODB_USER) +
          ":" +
          getString(VariablesRegistry.SETTINGS_MONGODB_PASSWORD) +
          "@" +
          getString(VariablesRegistry.SETTINGS_MONGODB_HOSTNAME),
      )

    database = Database(connection, dbName)
    database.connect()

    loadCollections()
  }

  /** Disconnect from database  */
  fun unload() {
    // Disconnect from database
    database.disconnect()
  }

  /**
   * Drops database and reloads collections
   */
  @JvmStatic
  fun drop() {
    database.drop()
  }

  /**
   * Add new profile into collection
   *
   * @param profile profile to add
   * @param collectionName name of collection
   */
  fun addProfile(profile: AbstractProfile, collectionName: String?) {
    // Get all keys from profile
    val keys = profile.keys

    val document = Document("_id", profile.id)

    for (key in keys) {
      // Add key and value to document
      document.append(key, profile[key])
    }

    // Insert document
    database.insertDocument(collectionName!!, document)
  }

  /**
   * Update profile field
   *
   * @param profile profile
   * @param collectionName name of collection
   * @param key key for value
   */
  fun updateProfileValue(
    profile: AbstractProfile,
    collectionName: String,
    key: String,
  ) {
    // Add key and value to document

    val document = Updates.set(key, profile[key])

    // Update a document
    database.updateDocument(collectionName, document, "_id", profile["_id"])
  }

  /**
   * Gets profile for player
   *
   * @param player player instance
   * @return PlayerProfile object
   */
  @JvmStatic
  fun getPlayerProfile(player: Player): PlayerProfile? = getPlayerProfile("uuid", player.uuid.toString())

  /**
   * Gets profile for player by search in players collection
   *
   * @param key key for value
   * @param value value
   * @return PlayerProfile object
   */
  fun getPlayerProfile(key: String, value: Any): PlayerProfile? {
    // Load document
    val playerDocument = loadDocument("players", key, value) ?: return null

    // Creating new player profile
    val playerProfile =
      PlayerProfile(
        playerDocument["_id"] as ObjectId,
        playerDocument.getString("uuid"),
        playerDocument.getString("username"),
        null,
        playerDocument["permissions"]!!.safeCastToList<String>(),
        playerDocument["rating"].safeCastToArray(),
        playerDocument["friends"]!!.safeCastToList<String>(),
      )

    var settingsDocument = getSettingsProfile(playerDocument)

    if (settingsDocument == null) {
      settingsDocument = createSettings(playerProfile).deserialize()
    }

    playerProfile.setSettings(serialize(settingsDocument, playerProfile))

    // Set document version to profile
    playerProfile.version = (playerDocument["version"] as Number?)!!.toLong()

    return playerProfile
  }

  /**
   * Gets settings for player by player document
   *
   * @param playerDocument document of player
   * @return Document with player settings
   */
  private fun getSettingsProfile(playerDocument: Document): Document? = playerDocument["settings"] as Document?

  /**
   * Gets player punishment profiles
   *
   * @param uuid player uuid
   * @return List with punishments
   */
  fun getPunishmentProfiles(uuid: UUID): List<PunishmentProfile> {
    // Get documents
    return getPunishmentProfiles("uuid", uuid.toString())
  }

  /**
   * Gets player punishment profile
   *
   * @param id document id
   * @return PunishmentProfile object
   */
  fun getPunishmentProfile(id: String): PunishmentProfile? {
    val objectId: ObjectId

    try {
      objectId = ObjectId(id)
    } catch (e: IllegalArgumentException) {
      // Logging the exception
      LOGGER.debug("Invalid ObjectId: $id$e")

      // You can throw a new exception or perform other handling if necessary
      return null
    }

    // Get documents
    val document = loadDocument("punishments", "_id", objectId) ?: return null

    return getPunishmentProfileFromDocument(document)
  }

  /**
   * Getting RoundProfile from database with specific key-value pair
   *
   * @param key Filter key
   * @param value Filter key value
   * @return Object from database or NULL
   */
  fun getRoundProfile(key: String, value: String): RoundProfile? {
    // Get documents
    val document = loadDocument("rounds", key, value) ?: return null

    return RoundProfile.getRoundProfileFromDocument(document)
  }

  /** Cancel all rounds  */
  @JvmStatic
  fun resetRounds() {
    val status = "roundStatus"
    val collection = database.getCollection("rounds")
    collection.updateMany(
      Filters.or(
        Filters.eq(status, "initializing"),
        Filters.eq(status, "preparing"),
        Filters.eq(status, "started"),
      ),
      Updates.set(status, "cancelled"),
    )
  }

  /**
   * Find profiles in punishments collection by key and value
   *
   * @param key key
   * @param value value
   * @return List with punishments
   */
  fun getPunishmentProfiles(key: String, value: Any): List<PunishmentProfile> {
    // Get documents
    val documents = loadDocuments("punishments", key, value)

    if (documents.first() == null) {
      return emptyList()
    }

    val profiles: MutableList<PunishmentProfile> = ArrayList()

    for (document in documents) {
      // Get profile from document
      val profile = getPunishmentProfileFromDocument(document)

      // Add punishment profile in profiles list
      profiles.add(profile)
    }

    return profiles
  }

  /** Reload database collections  */
  @JvmStatic
  fun reload() {
    // Load Collections
    loadCollections()
  }

  private fun loadCollections() {
    val collectionsNames = mainConfig.getStringList("collections")

    // Check is collection names assigned
    if (collectionsNames.isNullOrEmpty()) {
      stopServerWithError(RuntimeException("collection names is not assigned"))
    }

    // Load collections from config
    for (collectionName in collectionsNames) {
      loadCollection(collectionName)
    }
  }

  private fun loadCollection(name: String) {
    try {
      database.getCollection(name)
    } catch (_: IllegalArgumentException) {
      // Ignored exception because we only want to create collection if not exist.
      database.createCollection(name)
    }
  }

  private fun getPunishmentProfileFromDocument(document: Document): PunishmentProfile {
    // Create new punishment profile
    val profile =
      PunishmentProfile(
        document["_id"] as ObjectId?,
        document.getString("uuid"),
        document.getString("moderatorUuid"),
        document.getString("type"),
        document.getString("reason"),
        document.getString("internalNote"),
        document.getBoolean("isForceExpired"),
        (document["duration"] as Number?)!!.toLong(),
        Timestamp((document["applyTime"] as Date).time),
        Timestamp((document["startTime"] as Date).time),
        Timestamp((document["expireTime"] as Date).time),
      )
    // Set document version to profile
    profile.version = (document["version"] as Number?)!!.toLong()

    return profile
  }

  private fun loadDocument(
    collectionName: String,
    filterKey: String,
    filterValue: Any,
  ): Document? {
    // Get player profile documents by uuid
    return loadDocuments(collectionName, filterKey, filterValue).first()
  }

  private fun loadDocuments(
    collectionName: String,
    filterKey: String,
    filterValue: Any,
  ): FindIterable<Document> {
    // Get player profile documents by uuid
    return database.getCollection(collectionName).find(Filters.eq(filterKey, filterValue))
  }
}
