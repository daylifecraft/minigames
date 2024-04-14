package com.daylifecraft.common.variable

class VariablesRegistry {
  private val _requiredVariablesNames: MutableSet<String> = HashSet()
  private val _variablesNames: MutableSet<String> = HashSet()
  private val _defaults: MutableMap<String, String> = HashMap()

  val requiredVariablesNames: Set<String> = _requiredVariablesNames
  val variablesNames: Set<String> = _variablesNames
  val defaults: Map<String, String> = _defaults

  init {
    _variablesNames.add(SETTINGS_MONGODB_HOSTNAME)
    _variablesNames.add(SETTINGS_MONGODB_USER)
    _variablesNames.add(SETTINGS_MONGODB_PASSWORD)
    _variablesNames.add(SETTINGS_MONGODB_DATABASE)
    _variablesNames.add(SETTINGS_SERVER_ENV)
    _variablesNames.add(SETTINGS_VELOCITY_SECRET)
    _variablesNames.add(SETTINGS_OPERATION_MODE)
    _variablesNames.add(SETTINGS_DEBUG_COMMANDS)

    // Set required variables
    _requiredVariablesNames.add(SETTINGS_MONGODB_HOSTNAME)
    _requiredVariablesNames.add(SETTINGS_MONGODB_PASSWORD)

    // Set default variables
    _defaults[SETTINGS_MONGODB_USER] = "minigames"
    _defaults[SETTINGS_MONGODB_DATABASE] = "minigames"
    _defaults[SETTINGS_SERVER_ENV] = "PROD"
    _defaults[SETTINGS_DEBUG_COMMANDS] = "null"
  }

  companion object {
    const val SETTINGS_MONGODB_HOSTNAME = "DAYLIFECRAFT_SETTINGS_MONGODB_HOSTNAME"
    const val SETTINGS_MONGODB_USER = "DAYLIFECRAFT_SETTINGS_MONGODB_USER"
    const val SETTINGS_MONGODB_PASSWORD = "DAYLIFECRAFT_SETTINGS_MONGODB_PASSWORD"
    const val SETTINGS_MONGODB_DATABASE = "DAYLIFECRAFT_SETTINGS_MONGODB_DATABASE"
    const val SETTINGS_SERVER_ENV = "DAYLIFECRAFT_SETTINGS_SERVER_ENV"
    const val SETTINGS_VELOCITY_SECRET = "DAYLIFECRAFT_SETTINGS_VELOCITY_SECRET"
    const val SETTINGS_OPERATION_MODE = "DAYLIFECRAFT_SETTINGS_OPERATION_MODE"
    const val SETTINGS_DEBUG_COMMANDS = "DAYLIFECRAFT_SETTINGS_DEBUG_COMMANDS"
  }
}
