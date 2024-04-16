package com.daylifecraft.minigames.chat

import com.daylifecraft.minigames.ChatManager
import com.daylifecraft.minigames.Init.setupChatManager
import com.daylifecraft.minigames.UtilsForTesting
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.permission.Permission
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class PrivateMiniMessageTest {
  @Test
  @Order(1)
  fun testPlayerFormattedMessage() {
    MinecraftServer.getCommandManager().execute(fakePlayer1, "w PMTest2 $MESSAGE")

    verify {
      chatManager.sendPrivateMessage(
        fakePlayer1,
        fakePlayer2,
        any(),
        any(),
        MiniMessage.miniMessage().stripTags(MESSAGE),
      )
    }
  }

  @Test
  @Order(2)
  fun testPlayerMessage() {
    fakePlayer1.addPermission(Permission("isAdmin"))
    MinecraftServer.getCommandManager().execute(fakePlayer1, "w PMTest2 $MESSAGE")

    verify {
      chatManager.sendPrivateMessage(
        fakePlayer1,
        fakePlayer2,
        any(),
        any(),
        MESSAGE,
      )
    }
  }

  @AfterEach
  fun clearInvocations() {
    clearMocks(chatManager)
  }

  companion object {
    private lateinit var fakePlayer1: Player
    private lateinit var fakePlayer2: Player
    private const val MESSAGE = "<b>Test</b> <blue>message</blue>"

    private val chatManager = mockk<ChatManager>(relaxed = true)

    @BeforeAll
    @JvmStatic
    fun start() {
      fakePlayer1 = UtilsForTesting.initFakePlayer("PMTest1")
      fakePlayer2 = UtilsForTesting.initFakePlayer("PMTest2")

      UtilsForTesting.waitUntilPlayerJoin(fakePlayer1, fakePlayer2)

      setupChatManager(chatManager)
    }

    @AfterAll
    @JvmStatic
    fun kickPlayers() {
      fakePlayer1.kick("")
      fakePlayer2.kick("")
    }
  }
}
