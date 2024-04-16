package com.daylifecraft.minigames.chat

import com.daylifecraft.minigames.ChatManager
import com.daylifecraft.minigames.Init.setupChatManager
import com.daylifecraft.minigames.instance.AbstractCraftInstance
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class InstancesChatTest {
  @Test
  fun testChatInOneInstance() {
    every { instance.players } returns setOf(fakePlayer1, fakePlayer2)

    abstractCraftInstance.sendInstanceChatMessage(fakePlayer1, TEST_MESSAGE)

    verify {
      chatManager.sendPlayerChatMessage(
        fakePlayer1,
        fakePlayer2,
        any(),
      )
    }
  }

  @Test
  fun testChatInDifferentInstances() {
    every { instance.players } returns setOf(fakePlayer1)

    abstractCraftInstance.sendInstanceChatMessage(fakePlayer1, TEST_MESSAGE)

    verify(exactly = 0) {
      chatManager.sendPlayerChatMessage(
        fakePlayer1,
        fakePlayer2,
        any(),
      )
    }
  }

  @AfterEach
  fun clearInvocations() {
    clearMocks(chatManager)
  }

  companion object {
    private val chatManager = mockk<ChatManager>(relaxed = true)

    private val abstractCraftInstance = spyk<AbstractCraftInstance>()

    private val instance = mockk<Instance>(relaxed = true)

    private val fakePlayer1 = mockk<Player>(relaxed = true)
    private val fakePlayer2 = mockk<Player>(relaxed = true)

    private const val TEST_MESSAGE = "Some message"

    @BeforeAll
    @JvmStatic
    fun setup() {
      setupChatManager(chatManager)

      every { abstractCraftInstance.instance } returns instance
      every { abstractCraftInstance.canReceiveMessageFrom(any(), any()) } returns true
    }
  }
}
