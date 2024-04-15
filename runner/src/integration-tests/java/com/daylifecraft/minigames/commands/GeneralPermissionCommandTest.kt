package com.daylifecraft.minigames.commands;

import com.daylifecraft.minigames.UtilsForTesting;
import com.daylifecraft.minigames.config.ConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GeneralPermissionCommandTest {
  private static Player player;
  private final String testCommands =
    """
      COMMAND,         PERMISSION
      kick,        punishments.kick
      mute temporary,   punishments.mute.temp
      mute permanent,   punishments.mute.perm
      ban temporary,    punishments.ban.temp
      ban permanent,    punishments.ban.perm
      punishment list,  punishments.list
      punishment view,  punishments.view
      punishment set-note, punishments.notes.set
      punishment force-expire,  punishments.expire
      rounds view-details, rounds.view-details
      rounds force-end, rounds.force-end
      rounds view, rounds.view
      rounds spectate, rounds.spectate
      rounds say, rounds.say
      """;

  @BeforeAll
  static void start() throws InterruptedException {
    player = UtilsForTesting.initFakePlayer("CMTest");

    UtilsForTesting.waitUntilPlayerJoin(player);
  }

  @Order(1)
  @ParameterizedTest(name = "[{index}] {arguments}")
  @CsvSource(useHeadersInDisplayName = true, textBlock = testCommands)
  void testCommandInsufficientPerm(final String command, final String permission) {
    try {
      final Command finalCommand = getLastVar(command, permission);
      Assertions.assertFalse(
        finalCommand.getCondition().canUse(player, null), "Check if command can't be executed");
    } catch (final Exception exception) {
      exception.printStackTrace();
    }
  }

  @Order(2)
  @ParameterizedTest(name = "[{index}] {arguments}")
  @CsvSource(useHeadersInDisplayName = true, textBlock = testCommands)
  void testCommandSufficientPerm(final String command, final String permission) {
    final Command finalCommand = getLastVar(command, permission);
    player.addPermission(new Permission("isPlayerModerator"));
    Assertions.assertTrue(
      finalCommand.getCondition().canUse(player, null), "Check if command can be executed");
  }

  private Command getLastVar(final String command, final String permission) {
    final var subcommands = command.split(" ");

    final ArrayList<String> commands = new ArrayList<>();
    commands.add(permission);

    final List<Map<String, Object>> tempGroupData =
      ConfigManager.getMainConfig().getValueList("groups");
    tempGroupData.getFirst().put("permissions", commands);
    ConfigManager.getMainConfig().setValue("groups", tempGroupData);

    final Command com = MinecraftServer.getCommandManager().getCommand(subcommands[0]);
    Command finalCommand = com;
    for (var i = 1; i < subcommands.length; i++) {
      for (final Command c : com.getSubcommands()) {
        if (subcommands[i].equals(c.getName())) {
          finalCommand = c;

          break;
        }
      }
    }
    return finalCommand;
  }

  @AfterAll
  static void kickPlayers() {
    player.kick("");
  }
}
