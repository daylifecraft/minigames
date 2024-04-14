package com.daylifecraft.minigames.commands;

import com.daylifecraft.minigames.UtilsForTesting;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CommandRegistrationTest {
  private static Player player, player2;

  @BeforeAll
  static void start() throws InterruptedException {
    player = UtilsForTesting.initFakePlayer("Bot");
    player.addPermission(new Permission("isAdmin"));

    player2 = UtilsForTesting.initFakePlayer("Bot2");

    UtilsForTesting.waitUntilPlayerJoin(player, player2);
  }

  @ParameterizedTest(name = "[{index}] COMMAND registration {arguments}")
  @ValueSource(
    strings = {
      "ban",
      "mute",
      "kick",
      "punishment",
      "w",
      "friends",
      "confirm-action",
      "group",
      "rounds"
    })
  void testCommandRegistration(final String command) {
    Assertions.assertTrue(
      MinecraftServer.getCommandManager().commandExists(command),
      "test if the command is registered");
  }

  @ParameterizedTest(name = "[{index}] {arguments}")
  @CsvSource(
    useHeadersInDisplayName = true,
    textBlock =
      """

        COMMAND, CORRECT,  INCORRECT
        mute temp, NULL, Bot3 a b
        mute perm, Bot2 a, Bot3  a
        punishment list,NULL,NULL
        punishment view,NULL,NULL
        punishment set-note,NULL,NULL
        punishment force-expire,NULL,NULL
        confirm-action ,NULL,NULL
        friends add, Bot2, Bot3
        friends remove,Bot2, Bot4
        g,NULL,NULL
        group invite, Bot2, Bot3
        group accept,NULL,NULL
        group kick,NULL,NULL
        group new-leader,NULL,NULL
        group leave,NULL,NULL
        kick,  Bot2 a, Bot3 a
        ban temp,NULL, Bot3 a b
        ban perm,  Bot2 a, Bot3  a
        rounds view-details,507f191e810c19729de860ea,NULL
        rounds force-end,507f191e810c19729de860ea,NULL
        rounds view, Bot2, Bot3
        rounds spectate,507f191e810c19729de860ea,NULL
        rounds spectate,Bot2,Bot3
        """,
    nullValues = "NULL")
  void testCommandExecution(
    final String command, final String correctEnding, final String incorrectEnding) {
    Assertions.assertNotEquals(
      CommandResult.Type.UNKNOWN,
      MinecraftServer.getCommandManager().execute(player, command).getType(),
      "executing a normal command without attributes");
    if (correctEnding != null) {
      Assertions.assertEquals(
        CommandResult.Type.SUCCESS,
        MinecraftServer.getCommandManager()
          .execute(player, command + " " + correctEnding)
          .getType(),
        "Test if the correct command is executed normally");
    }
    if (incorrectEnding != null) {
      Assertions.assertNotEquals(
        CommandResult.Type.UNKNOWN,
        MinecraftServer.getCommandManager()
          .execute(player, command + " " + incorrectEnding)
          .getType(),
        "Test if the incorrect command is executed without exceptions");
    }
  }

  @AfterAll
  static void kickPlayers() {
    player.kick("");
    if (player2 != null) {
      player2.kick("");
    }
  }
}
