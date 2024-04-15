package com.daylifecraft.minigames

import com.daylifecraft.common.variable.VariablesManager
import com.daylifecraft.common.variable.VariablesRegistry
import com.daylifecraft.minigames.Init.miniGamesSettingsManager
import com.daylifecraft.minigames.Init.stopServer
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.config.ConfigManager
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.gui.GuiManager
import com.daylifecraft.minigames.listener.ListenerManager
import io.mockk.mockk
import net.minestom.server.MinecraftServer
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.atomic.AtomicBoolean

class RunServerExtension :
  BeforeAllCallback,
  ExtensionContext.Store.CloseableResource {

  override fun beforeAll(context: ExtensionContext) {
    if (!started.getAndSet(true)) {
      context.root.getStore(ExtensionContext.Namespace.GLOBAL).put("RunServer", this)

      Init.enableTests()

      Init.initVariablesManager()

      Init.initLogger()

      ShutdownHook.init()

      ConfigManager.load()

      val dbName = VariablesManager.getString(VariablesRegistry.SETTINGS_MONGODB_DATABASE) + "-tests"

      DatabaseManager.load(dbName)
      DatabaseManager.drop()
      DatabaseManager.reload()

      val minecraftServer = MinecraftServer.init()
      ListenerManager.load(MinecraftServer.getGlobalEventHandler())
      CommandsManager.load(MinecraftServer.getCommandManager(), true)

      Init.loadCraftInstances()
      Init.setupChatManager()
      Init.initLang()

      val mockedGuiManager = mockk<GuiManager>(relaxed = true)
      Init.setupGuiManager(mockedGuiManager)

      // Load mini games
      Init.setupMiniGamesManager()
      miniGamesSettingsManager!!.onStartupLoad()

      minecraftServer.start("::", 16666)

      // Load supported
      UtilsForTesting.loadOnStartup()
    }
  }

  override fun close() {
    stopServer()
    started.set(false)
  }

  companion object {
    private val started = AtomicBoolean(false)
  }
}
