package com.daylifecraft.minigames.debug;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.CommandResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DebugCommandExecutionTest {

  @BeforeAll
  static void registerDebugCommand() {
    Assertions.assertTrue(
      MinecraftServer.getCommandManager().commandExists("~"),
      "Assert that debug command is registered");

    MinecraftServer.getCommandManager().getCommand("~").addSubcommand(new DebugTestSubCommand());
  }

  @Test
  void testDebugCommandExecutes() {
    final CommandResult result = MinecraftServer.getCommandManager().executeServerCommand("~");
    Assertions.assertEquals(
      "debug.command.usage",
      result.getCommandData().getDataMap().get("value"),
      "test for the correct ~ command");
  }

  @Test
  void testIncorrectUseDebugSubCommand() {
    // test for the incorrect command
    final var result = MinecraftServer.getCommandManager().executeServerCommand("~ test");
    Assertions.assertEquals(
      "debug.command.fail.general",
      result.getCommandData().getDataMap().get("value"),
      "test for the incorrect subcommand");
  }

  @Test
  void testCorrectUseDebugSubCommand() {
    // test for the correct command
    final var result = MinecraftServer.getCommandManager().executeServerCommand("~ test 2");
    Assertions.assertEquals(
      "correct_command",
      result.getCommandData().getDataMap().get("value"),
      "test for the correct subcommand");
  }
}
