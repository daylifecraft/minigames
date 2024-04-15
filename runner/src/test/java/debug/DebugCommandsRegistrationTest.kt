package debug;

import com.daylifecraft.minigames.Init;
import com.daylifecraft.minigames.command.CommandsManager;
import com.daylifecraft.minigames.command.debug.DebugCommand;
import net.minestom.server.command.CommandManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class DebugCommandsRegistrationTest {

  private static CommandManager commandManager;

  @BeforeAll
  static void setupCommandManager() {
    Init.enableTests();

    commandManager = Mockito.mock(CommandManager.class);
    Mockito.doNothing().when(commandManager).register(ArgumentMatchers.any());
  }

  @Test
  void testIfDebugCommandDoesNotRegistered() {
    CommandsManager.load(commandManager, false);
    Mockito.verify(commandManager, Mockito.never())
      .register(ArgumentMatchers.isA(DebugCommand.class));
  }

  @Test
  void testIfDebugCommandsRegistered() {
    CommandsManager.load(commandManager, true);
    Mockito.verify(commandManager).register(ArgumentMatchers.isA(DebugCommand.class));
  }

  @AfterEach
  void clearMockCalls() {
    Mockito.clearInvocations(commandManager);
  }
}
