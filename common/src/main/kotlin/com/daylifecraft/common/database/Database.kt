package com.daylifecraft.common.database

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import org.bson.BsonDocument
import org.bson.BsonInt64
import org.bson.Document
import org.bson.conversions.Bson

class Database(
  private val connectionString: ConnectionString,
  private val databaseName: String,
) {
  private lateinit var mongoDatabase: MongoDatabase
  private lateinit var client: MongoClient

  /** Connect database into mongo  */
  fun connect() {
    // Connecting to mongo database

    client = MongoClients.create(connectionString)

    // Get database
    mongoDatabase = client.getDatabase(databaseName)

    // Check is it connected to database
    mongoDatabase.runCommand(BsonDocument("ping", BsonInt64(1)))
  }

  /** Disconnect database from mongo  */
  fun disconnect() =
    client.close()

  /**
   * Drops database
   */
  fun drop() {
    mongoDatabase.drop()
  }

  /**
   * Get collection by name from database
   *
   * @param name collection name
   */
  fun getCollection(name: String): MongoCollection<Document> {
    for (existName in mongoDatabase.listCollectionNames()) {
      // Check does collection name exist in database
      if (existName == name) {
        return mongoDatabase.getCollection(name)
      }
    }

    throw IllegalArgumentException("this collection does not exist")
  }

  /**
   * Create new collection by name from database
   *
   * @param name collection name
   */
  fun createCollection(name: String): MongoCollection<Document> {
    // Create new collection
    mongoDatabase.createCollection(name)
    // Get created collection
    val collection = getCollection(name)

    return collection
  }

  /**
   * Insert document into database collection
   *
   * @param collectionName collection name
   * @param document document to insert
   */
  fun insertDocument(collectionName: String, document: Document): InsertOneResult = getCollection(collectionName).insertOne(document)

  /**
   * Update document field in database collection
   *
   * @param collectionName collection name
   * @param document document to insert
   * @param key key for value
   * @param value value to update
   */
  fun updateDocument(
    collectionName: String,
    document: Bson,
    key: String,
    value: Any?,
  ): UpdateResult {
    // Get collection
    val collection = getCollection(collectionName)

    // Update document in collection
    return collection.updateOne(Filters.eq(key, value), document, UpdateOptions().upsert(true))
  }
}
