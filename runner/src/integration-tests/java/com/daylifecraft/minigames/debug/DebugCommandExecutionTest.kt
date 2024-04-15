package com.daylifecraft.minigames.debug

import net.minestom.server.MinecraftServer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DebugCommandExecutionTest {
  @Test
  fun testDebugCommandExecutes() {
    val result = MinecraftServer.getCommandManager().executeServerCommand("~")
    assertEquals(
      expected = "debug.command.usage",
      actual = result.commandData!!.dataMap["value"],
      message = "test for the correct ~ command",
    )
  }

  @Test
  fun testIncorrectUseDebugSubCommand() {
    // test for the incorrect command
    val result = MinecraftServer.getCommandManager().executeServerCommand("~ test")
    assertEquals(
      expected = "debug.command.fail.general",
      actual = result.commandData!!.dataMap["value"],
      message = "test for the incorrect subcommand",
    )
  }

  @Test
  fun testCorrectUseDebugSubCommand() {
    // test for the correct command
    val result = MinecraftServer.getCommandManager().executeServerCommand("~ test 2")
    assertEquals(
      expected = "correct_command 3",
      actual = result.commandData!!.dataMap["value"],
      message = "test for the correct subcommand",
    )
  }

  companion object {
    @BeforeAll
    @JvmStatic
    fun registerDebugCommand() {
      assertTrue(
        MinecraftServer.getCommandManager().commandExists("~"),
        message = "Assert that debug command is registered",
      )

      MinecraftServer.getCommandManager().getCommand("~")!!.addSubcommand(DebugTestSubCommand())
    }
  }
}
