package com.daylifecraft.common.exception

import com.daylifecraft.common.variable.Variable

class EnvironmentVariableNullException(variable: Variable) : RuntimeException("Environment variable <" + variable.name + "> is not assigned")
