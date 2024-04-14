package com.daylifecraft.common.variable

object VariablesManager {
  private val variables: MutableMap<String, Variable> = HashMap()
  private val variablesRegistry = VariablesRegistry()

  /** Loads variables and adds them to the manager  */
  fun load() {
    variablesRegistry.variablesNames.forEach(VariablesManager::addVariable)
  }

  /**
   * Gets a String variable from the manager
   *
   * @param name name of variable
   * @return String variable
   */
  fun getString(name: String): String? =
    variables[name]?.value

  private fun addVariable(name: String) {
    val variable = Variable(
      name = name,
      defaultValue = variablesRegistry.defaults[name],
      isRequired = variablesRegistry.requiredVariablesNames.contains(name),
    )

    variable.load()

    variables[variable.name] = variable
  }
}
