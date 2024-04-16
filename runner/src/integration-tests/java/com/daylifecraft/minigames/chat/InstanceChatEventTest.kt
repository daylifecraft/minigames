package com.daylifecraft.minigames.chat

import com.daylifecraft.minigames.ChatManager
import com.daylifecraft.minigames.Init.setupChatManager
import com.daylifecraft.minigames.UtilsForTesting
import io.mockk.mockk
import io.mockk.verify
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.player.PlayerChatEvent
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class InstanceChatEventTest {
  @Test
  fun testDoesEventCalled() {
    val event = PlayerChatEvent(fakePlayer, emptyList(), { Component.empty() }, "Some text")
    EventDispatcher.call(event)

    assertTrue(event.isCancelled, "Event must me cancelled")

    verify {
      chatManager.sendPlayerChatMessage(
        fakePlayer,
        fakePlayer,
        any(),
      )
    }
  }

  companion object {
    private lateinit var fakePlayer: Player

    private val chatManager = mockk<ChatManager>(relaxed = true)

    @BeforeAll
    @JvmStatic
    fun start() {
      setupChatManager(chatManager)

      fakePlayer = UtilsForTesting.initFakePlayer("EventsTest")

      UtilsForTesting.waitUntilPlayerJoin(fakePlayer)
    }

    @AfterAll
    @JvmStatic
    fun kickPlayer() {
      fakePlayer.kick("")
    }
  }
}
