package com.daylifecraft.minigames.debug

import com.daylifecraft.common.variable.VariablesManager
import com.daylifecraft.common.variable.VariablesRegistry
import com.daylifecraft.minigames.Init.isInDebugMode
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DebugStatusTest {
  @Test
  fun testIsDebugCommandOn() {
    mockkObject(VariablesManager)
    every { VariablesManager.getString(VariablesRegistry.SETTINGS_DEBUG_COMMANDS) } returns "true"

    assertTrue(isInDebugMode, "Expected that debug commands was enabled")
  }

  @Test
  fun testIsDebugCommandOff() {
    mockkObject(VariablesManager)
    every { VariablesManager.getString(VariablesRegistry.SETTINGS_DEBUG_COMMANDS) } returns "false"

    assertFalse(isInDebugMode, "Expected that debug commands was disabled")
  }

  @Test
  fun testIsDevModeOn() {
    mockkObject(VariablesManager)
    every { VariablesManager.getString(VariablesRegistry.SETTINGS_DEBUG_COMMANDS) } returns null
    every { VariablesManager.getString(VariablesRegistry.SETTINGS_OPERATION_MODE) } returns "DEV"

    assertTrue(isInDebugMode, "Expected that DEV mode enabled!")
  }
}
