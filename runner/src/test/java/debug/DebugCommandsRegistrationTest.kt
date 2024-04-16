package debug

import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.debug.DebugCommand
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import net.minestom.server.command.CommandManager
import org.junit.jupiter.api.Test

internal class DebugCommandsRegistrationTest {

  @RelaxedMockK
  private lateinit var commandManager: CommandManager

  @Test
  fun testIfDebugCommandDoesNotRegistered() {
    CommandsManager.load(commandManager, false)

    verify(exactly = 0) { commandManager.register(ofType<DebugCommand>()) }
  }

  @Test
  fun testIfDebugCommandsRegistered() {
    CommandsManager.load(commandManager, true)

    verify { commandManager.register(ofType<DebugCommand>()) }
  }
}
