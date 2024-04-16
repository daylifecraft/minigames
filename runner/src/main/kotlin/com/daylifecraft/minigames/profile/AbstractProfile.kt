package com.daylifecraft.minigames.profile

import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.text.i18n.Language
import org.bson.types.ObjectId

abstract class AbstractProfile protected constructor(
  id: ObjectId,
  version: Long,
  private val collection: String,
) {
  val keys: Set<String>
    get() = parameters.keys
  var id: ObjectId? = id
    private set

  var version: Long = version
    set(value) {
      set("version", value)
      field = value
    }

  protected var parameters: MutableMap<String, Any?> = LinkedHashMap()

  init {
    parameters["_id"] = id
    parameters["version"] = version
  }

  abstract fun getDetails(language: Language): AbstractProfileDetails?

  operator fun get(key: String): Any? = parameters[key]

  fun setWithoutUpdate(parameter: String, value: Any) {
    parameters[parameter] = value
  }

  open fun set(parameter: String, value: Any) {
    parameters[parameter] = value
    DatabaseManager.updateProfileValue(this, collection, parameter)
  }

  fun setAll(parameters: MutableMap<String, Any?>) {
    this.parameters = parameters
  }
}
