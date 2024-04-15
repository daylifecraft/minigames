package com.daylifecraft.minigames

import com.daylifecraft.common.logging.building.Logger
import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.logging.foundation.LogEvent
import com.daylifecraft.common.metrics.MinestomMetrics
import com.daylifecraft.common.variable.VariablesManager
import com.daylifecraft.common.variable.VariablesRegistry
import com.daylifecraft.minigames.ServerUuidProvider.uuid
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.config.ConfigManager
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.gui.GuiManager
import com.daylifecraft.minigames.instance.CraftInstancesManager
import com.daylifecraft.minigames.instance.instances.lobby.LobbyInstance
import com.daylifecraft.minigames.listener.ListenerManager
import com.daylifecraft.minigames.metrics.RunnerMetrics
import com.daylifecraft.minigames.minigames.MiniGamesInstancesCleaner
import com.daylifecraft.minigames.minigames.RoundPlayersSearcher
import com.daylifecraft.minigames.minigames.controllers.MiniGameControllersManager
import com.daylifecraft.minigames.minigames.settings.MiniGamesSettingManager
import com.daylifecraft.minigames.seasons.SeasonsManager
import com.daylifecraft.minigames.text.i18n.Lang
import io.prometheus.metrics.exporter.httpserver.HTTPServer
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics
import net.minestom.server.MinecraftServer
import net.minestom.server.extras.MojangAuth
import net.minestom.server.extras.velocity.VelocityProxy
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.system.exitProcess

private const val SERVER_IP = "::"

object Init {
  private const val ONLINE_STATUS = "online"
  private const val OFFLINE_STATUS = "offline"
  private const val DEV_STATUS = "DEV"

  private lateinit var logger: Logger

  var craftInstancesManager: CraftInstancesManager? = null
    private set

  var chatManager: ChatManager? = null
    private set

  @JvmStatic
  var miniGamesSettingsManager: MiniGamesSettingManager? = null
    private set

  var onlineMode: Boolean = false
    private set

  lateinit var guiManager: GuiManager
    private set

  lateinit var miniGameControllersManager: MiniGameControllersManager
    private set

  var isInsideTests: Boolean = false
    private set

  val serverEnv: String?
    get() = VariablesManager.getString(VariablesRegistry.SETTINGS_SERVER_ENV)

  private val serverOperationMode: String?
    get() = VariablesManager.getString(VariablesRegistry.SETTINGS_OPERATION_MODE)

  @JvmStatic
  val isInDebugMode: Boolean
    get() {
      // Get DEBUG_COMMANDS from environment variable
      val useDebugCommands = VariablesManager.getString(VariablesRegistry.SETTINGS_DEBUG_COMMANDS)
      when (useDebugCommands) {
        "true" -> return true
        "false" -> return false
      }

      return VariablesManager.getString(VariablesRegistry.SETTINGS_OPERATION_MODE) == "DEV"
    }

  @JvmStatic
  fun main(args: Array<String>) {
    try {
      initialize()
    } catch (e: Exception) {
      stopServerWithError(e)
    }
  }

  @JvmStatic
  fun stopServer() {
    MinecraftServer.stopCleanly()
  }

  fun stopServerWithError(throwable: Throwable): Nothing {
    logger.build(LogEvent.SERVER_CRASHED) {
      message("Server stopped with error: $throwable")
      message(throwable.message)
      details("stackTrace", throwable.stackTrace)
      details("cause", throwable.cause)
    }

    exitProcess(1)
  }

  @JvmStatic
  fun initVariablesManager() {
    VariablesManager.load()
  }

  @JvmStatic
  fun initLogger() {
    logger = createLogger()
  }

  @JvmStatic
  @Throws(IOException::class)
  fun initLang() {
    Lang.init()
  }

  @Throws(Exception::class)
  private fun initialize() {
    initVariablesManager()
    initLogger()
    initLicenseMode()

    // Create server stop handler
    ShutdownHook.init()

    // add uncaught exception handler
    Thread.currentThread().uncaughtExceptionHandler =
      Thread.UncaughtExceptionHandler { _, e: Throwable ->
        stopServerWithError(e)
      }

    logger.debug(
      """
      Initializing mini-games server $uuid
      SERVER_ENV="$serverEnv"
      """.trimIndent(),
    )

    // Load Configuration files
    ConfigManager.load()

    // Load seasons manager (depends on ConfigManager)
    SeasonsManager.load()

    // Enable velocity proxy
    val velocityProxyEnabled = enableVelocityProxy()

    // Load mongo database
    DatabaseManager.load()
    DatabaseManager.resetRounds()
    // Load languages
    initLang()

    // Chat manager
    setupChatManager()

    setupGuiManager()

    // Initialization
    val minecraftServer = MinecraftServer.init()

    // Load mini games
    setupMiniGamesManager()
    miniGamesSettingsManager!!.onStartupLoad()

    // Get instance manager
    loadCraftInstances()

    // Load mini games instances
    setupMiniGamesControllersManager()
    miniGameControllersManager.onStartupLoad()

    // Start round searcher
    RoundPlayersSearcher().setup()

    // Start MiniGames instances cleaner
    MiniGamesInstancesCleaner().setup()

    // Velocity can handle auth itself if enabled
    if (!velocityProxyEnabled) {
      if (onlineMode) {
        MojangAuth.init()
      } else {
        MinecraftServer.getConnectionManager().setUuidProvider { _, username: String ->
          UUID.nameUUIDFromBytes(username.toByteArray(StandardCharsets.UTF_8))
        }
      }
    }

    // Get global event handler manager
    val globalEventHandler = MinecraftServer.getGlobalEventHandler()

    // Load event listeners
    ListenerManager.load(globalEventHandler)

    // Load commands manager
    CommandsManager.load(MinecraftServer.getCommandManager(), isInDebugMode)

    // Start the server on port 25565
    minecraftServer.start(SERVER_IP, ConfigManager.mainConfig!!.getInt("ports.minecraft")!!)

    setupMetrics()

    logger.build(LogEvent.SERVER_STARTED) {
      message("Server successfully started")
      details("assignedServerUuid", uuid)
      details("serverIp", MinecraftServer.getServer().address)
      details("serverPort", MinecraftServer.getServer().port)
    }
  }

  @JvmStatic
  fun setupChatManager(toSetup: ChatManager?) {
    if (isInsideTests) {
      chatManager = toSetup
    } else {
      error("Setting GUIManager by method, which allows that ONLY FOR TESTS")
    }
  }

  @JvmStatic
  fun setupCraftInstancesManager(toSetup: CraftInstancesManager) {
    if (isInsideTests) {
      craftInstancesManager = toSetup
    } else {
      setOnlyForTestError("CraftInstancesManager")
    }
  }

  fun setupGuiManager(toSetup: GuiManager) {
    if (isInsideTests) {
      guiManager = toSetup
    } else {
      setOnlyForTestError("GuiManager")
    }
  }

  /** Loads the instances. Lobby, etc.  */
  fun loadCraftInstances() {
    craftInstancesManager = CraftInstancesManager(MinecraftServer.getInstanceManager())
    craftInstancesManager!!.addInstance(LobbyInstance())
  }

  /** Setup chat manager  */
  fun setupChatManager() {
    chatManager = ChatManager()
  }

  /** Setup GUI manager  */
  private fun setupGuiManager() {
    guiManager = GuiManager()
  }

  private fun setupMetrics() {
    JvmMetrics.builder().register()
    MinestomMetrics.builder().register()
    RunnerMetrics.builder().register()

    val prometheusPort = ConfigManager.mainConfig!!.getInt("ports.prometheus")!!
    HTTPServer.builder().port(prometheusPort).buildAndStart()
  }

  /** Setup mini games manager  */
  fun setupMiniGamesManager() {
    miniGamesSettingsManager = MiniGamesSettingManager()
  }

  fun enableTests() {
    isInsideTests = true
  }

  private fun setupMiniGamesControllersManager() {
    miniGameControllersManager = MiniGameControllersManager(miniGamesSettingsManager!!)
  }

  private fun enableVelocityProxy(): Boolean {
    // Get secret key from environment variable
    val secret = VariablesManager.getString(VariablesRegistry.SETTINGS_VELOCITY_SECRET)

    // Enable proxy only if secret is assigned
    if (secret.isNullOrEmpty()) return false

    VelocityProxy.enable(secret)
    logger.debug("Proxy was enabled: force enable only license mode")
    onlineMode = true

    return VelocityProxy.isEnabled()
  }

  /** Configures the license mode according to the settings.  */
  private fun initLicenseMode() {
    onlineMode = when (serverOperationMode) {
      ONLINE_STATUS -> true
      OFFLINE_STATUS -> false
      else -> serverEnv != DEV_STATUS
    }
  }

  private fun setOnlyForTestError(variable: String): Nothing {
    error("Setting $variable by method, which allows that ONLY FOR TESTS")
  }
}
