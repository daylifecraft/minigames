package com.daylifecraft.minigames.commands

import io.mockk.every
import io.mockk.mockk
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.CommandResult
import net.minestom.server.entity.Player
import net.minestom.server.permission.Permission
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

private const val COMMANDS = """
COMMAND,                  CORRECT,                   INCORRECT
mute temp,                NULL,                      Bot3 a b
mute perm,                Bot2 a,                    Bot3  a
punishment list,          NULL,                      NULL
punishment view,          NULL,                      NULL
punishment set-note,      NULL,                      NULL
punishment force-expire,  NULL,                      NULL
confirm-action,           NULL,                      NULL
friends add,              Bot2,                      Bot3
friends remove,           Bot2,                      Bot4
g,                        NULL,                      NULL
group invite,             Bot2,                      Bot3
group accept,             NULL,                      NULL
group kick,               NULL,                      NULL
group new-leader,         NULL,                      NULL
group leave,              NULL,                      NULL
kick,                     Bot2 a,                    Bot3 a
ban temp,                 NULL,                      Bot3 a b
ban perm,                 Bot2 a,                    Bot3  a
rounds view-details,      507f191e810c19729de860ea,  NULL
rounds force-end,         507f191e810c19729de860ea,  NULL
rounds view,              Bot2,                      Bot3
rounds spectate,          507f191e810c19729de860ea,  NULL
rounds spectate,          Bot2,                      Bot3
"""

internal class CommandRegistrationTest {
  @ParameterizedTest(name = "[{index}] COMMAND registration {arguments}")
  @ValueSource(
    strings = [
      "ban", "mute", "kick", "punishment", "w", "friends", "confirm-action", "group", "rounds",
    ],
  )
  fun testCommandRegistration(command: String) {
    assertTrue(
      MinecraftServer.getCommandManager().commandExists(command),
      message = "Command $command not found",
    )
  }

  @ParameterizedTest(name = "[{index}] {arguments}")
  @CsvSource(
    useHeadersInDisplayName = true,
    textBlock = COMMANDS,
    nullValues = ["NULL"],
  )
  fun testCommandExecution(
    command: String,
    correctEnding: String?,
    incorrectEnding: String?,
  ) {
    assertNotEquals(
      illegal = CommandResult.Type.UNKNOWN,
      actual = MinecraftServer.getCommandManager().execute(player, command).type,
      message = "Command $command must be registered",
    )

    if (correctEnding != null) {
      assertEquals(
        expected = CommandResult.Type.SUCCESS,
        actual = MinecraftServer.getCommandManager()
          .execute(player, "$command $correctEnding")
          .type,
        message = "Correct command /$command $correctEnding must return SUCCESS result",
      )
    }
    if (incorrectEnding != null) {
      assertNotEquals(
        illegal = CommandResult.Type.UNKNOWN,
        actual = MinecraftServer.getCommandManager()
          .execute(player, "$command $incorrectEnding")
          .type,
        message = "Incorrect command /$command $incorrectEnding must return UNKNOWN result",
      )
    }
  }

  companion object {
    private val player: Player = mockk(relaxed = true)

    @BeforeAll
    @JvmStatic
    fun start() {
      every { player.allPermissions } returns setOf(Permission("isAdmin"))
    }
  }
}
