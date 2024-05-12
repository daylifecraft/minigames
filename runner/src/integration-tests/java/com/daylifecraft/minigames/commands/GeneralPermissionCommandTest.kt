package com.daylifecraft.minigames.commands

import com.daylifecraft.minigames.config.ConfigManager
import com.daylifecraft.minigames.config.GroupConfig
import com.daylifecraft.minigames.config.MainConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player
import net.minestom.server.permission.Permission
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val TEST_COMMANDS = """
COMMAND,                  PERMISSION
kick,                     punishments.kick
mute temporary,           punishments.mute.temp
mute permanent,           punishments.mute.perm
ban temporary,            punishments.ban.temp
ban permanent,            punishments.ban.perm
punishment list,          punishments.list
punishment view,          punishments.view
punishment set-note,      punishments.notes.set
punishment force-expire,  punishments.expire
rounds view-details,      rounds.view-details
rounds force-end,         rounds.force-end
rounds view,              rounds.view
rounds spectate,          rounds.spectate
rounds say,               rounds.say
"""

internal class GeneralPermissionCommandTest {

  @ParameterizedTest(name = "[{index}] {arguments}")
  @CsvSource(useHeadersInDisplayName = true, textBlock = TEST_COMMANDS)
  fun testCommandInsufficientPerm(command: String, permission: String) {
    val player = mockk<Player>(relaxed = true)

    mockkObject(ConfigManager) {
      every { ConfigManager.mainConfig } returns configWithGroupWithPermission(permission)

      val finalCommand = getBestSubCommand(command)
      assertFalse(
        finalCommand.condition!!.canUse(player, null),
        message = "Command must not be executed because player have not permission",
      )
    }
  }

  @ParameterizedTest(name = "[{index}] {arguments}")
  @CsvSource(useHeadersInDisplayName = true, textBlock = TEST_COMMANDS)
  fun testCommandSufficientPerm(command: String, permission: String) {
    val player = mockk<Player>(relaxed = true)
    every { player.allPermissions } returns setOf(Permission("isPlayerModerator"))

    mockkObject(ConfigManager) {
      every { ConfigManager.mainConfig } returns configWithGroupWithPermission(permission)

      val finalCommand = getBestSubCommand(command)
      assertTrue(
        finalCommand.condition!!.canUse(player, null),
        message = "Command must be executed because player have permission",
      )
    }
  }
}

private fun configWithGroupWithPermission(permission: String): MainConfig {
  val mainConfig = mockk<MainConfig>()
  every { mainConfig.groups } returns listOf(
    GroupConfig(
      name = "isPlayerModerator",
      badge = "stub",
      permissions = listOf(permission),
      globalChatColor = null,
    ),
  )
  return mainConfig
}

/**
 * Returns best command node in commands tree for provided string
 * @throws [NullPointerException] when there is no matching command
 */
private fun getBestSubCommand(command: String): Command {
  val tokens = command.split(' ').dropLastWhile { it.isEmpty() }.toTypedArray()
  val currentCommand = MinecraftServer.getCommandManager().getCommand(tokens[0])!!

  return getBestSubCommand(tokens, 0, currentCommand)
}

private fun getBestSubCommand(tokens: Array<String>, currentToken: Int, currentCommand: Command): Command {
  if (currentToken + 1 >= tokens.size) return currentCommand

  for (subcommand in currentCommand.subcommands) {
    for (subcommandName in subcommand.names) {
      if (subcommandName == tokens[currentToken + 1]) {
        return getBestSubCommand(tokens, currentToken + 1, subcommand)
      }
    }
  }

  return currentCommand
}
