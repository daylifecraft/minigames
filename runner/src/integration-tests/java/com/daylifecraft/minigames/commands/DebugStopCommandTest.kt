package com.daylifecraft.minigames.commands;

import com.daylifecraft.minigames.Init;
import com.daylifecraft.minigames.ShutdownHook;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class DebugStopCommandTest {

  private static long lastShutdownReason = -1;

  @BeforeAll
  static void start() {
    final ShutdownHook shutdownHook = Mockito.mock(ShutdownHook.class);
    Mockito.doAnswer(
        answer -> {
          lastShutdownReason = answer.getArgument(0);

          return null;
        })
      .when(shutdownHook)
      .run(ArgumentMatchers.anyLong());

    Init.setupShutdownHook(shutdownHook);
  }

  @Test
  void testStopDryRun() {
    MinecraftServer.getCommandManager().executeServerCommand("~ stop dry-run");
    Assertions.assertEquals(
      1, lastShutdownReason, "assert that shutdown hook has been running by dry run");
  }

  @Test
  void testStopRun() {
    MinecraftServer.getCommandManager().executeServerCommand("~ stop");
    Assertions.assertEquals(0, lastShutdownReason, "assert that server has been stopped");
  }
}
