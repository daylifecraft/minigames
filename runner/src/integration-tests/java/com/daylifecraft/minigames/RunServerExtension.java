package com.daylifecraft.minigames;

import com.daylifecraft.common.variable.VariablesManager;
import com.daylifecraft.common.variable.VariablesRegistry;
import com.daylifecraft.minigames.command.CommandsManager;
import com.daylifecraft.minigames.config.ConfigManager;
import com.daylifecraft.minigames.database.DatabaseManager;
import com.daylifecraft.minigames.gui.GuiManager;
import com.daylifecraft.minigames.listener.ListenerManager;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class RunServerExtension
  implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

  private static volatile boolean started;

  @Override
  public void beforeAll(final ExtensionContext context) {
    if (!started) {
      started = true;
      context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put("RunServer", this);

      Init.enableTests();

      Init.initServerUuid();
      Init.initVariablesManager();

      Init.initLogger();

      Init.initShutdownHook();

      ConfigManager.load();

      var dbName = VariablesManager.INSTANCE.getString(VariablesRegistry.SETTINGS_MONGODB_DATABASE) + "-tests";

      DatabaseManager.load(dbName);
      DatabaseManager.drop();
      DatabaseManager.reload();

      final var minecraftServer = MinecraftServer.init();
      ListenerManager.load(MinecraftServer.getGlobalEventHandler());
      CommandsManager.load(MinecraftServer.getCommandManager(), true);

      Init.loadCraftInstances();
      Init.setupChatManager();

      final var mockedGuiManager = Mockito.mock(GuiManager.class);
      Mockito.doNothing().when(mockedGuiManager).tick(ArgumentMatchers.any());
      Init.setupGuiManager(mockedGuiManager);

      // Load mini games
      Init.setupMiniGamesManager();
      Init.getMiniGamesSettingsManager().onStartupLoad();

      minecraftServer.start("::", 16_666);

      // Load supported
      UtilsForTesting.loadOnStartup();
    }
  }

  @Override
  public void close() {
    Init.stopServer();
    started = false;
  }
}
