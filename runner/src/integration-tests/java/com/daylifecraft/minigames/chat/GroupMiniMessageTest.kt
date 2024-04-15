package com.daylifecraft.minigames.chat

import com.daylifecraft.common.instance.InstanceType
import com.daylifecraft.minigames.ChatManager
import com.daylifecraft.minigames.Init.setupChatManager
import com.daylifecraft.minigames.UtilsForTesting
import com.daylifecraft.minigames.instance.CraftInstancesManager.Companion.get
import com.daylifecraft.minigames.profile.group.PlayersGroup
import com.daylifecraft.minigames.profile.group.PlayersGroupManager.createGroup
import io.mockk.mockk
import io.mockk.verify
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.permission.Permission
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class GroupMiniMessageTest {
  @Test
  @Order(1)
  fun testPlayerFormattedMessage() {
    MinecraftServer.getCommandManager().execute(fakePlayer1, "g $MESSAGE")

    verify {
      chatManager.sendGroupChatMessage(
        fakePlayer1,
        playersGroup,
        MiniMessage.miniMessage().stripTags(MESSAGE),
      )
    }
  }

  @Test
  @Order(2)
  fun testPlayerMessage() {
    fakePlayer1.addPermission(Permission("isAdmin"))
    MinecraftServer.getCommandManager().execute(fakePlayer1, "g $MESSAGE")

    verify {
      chatManager.sendGroupChatMessage(
        fakePlayer1,
        playersGroup,
        MESSAGE,
      )
    }
  }

  companion object {
    private lateinit var fakePlayer1: Player
    private lateinit var fakePlayer2: Player
    private lateinit var playersGroup: PlayersGroup
    private const val MESSAGE = "<b>Test</b> <blue>message</blue>"

    private val chatManager = mockk<ChatManager>(relaxed = true)

    @BeforeAll
    @Throws(InterruptedException::class)
    @JvmStatic
    fun start() {
      val testInstance = get().getAnyInstanceByType(InstanceType.LOBBY)!!.instance
      fakePlayer1 = UtilsForTesting.initFakePlayer("GMTest1", testInstance)
      fakePlayer2 = UtilsForTesting.initFakePlayer("GMTest2", testInstance)

      UtilsForTesting.waitUntilPlayerJoin(fakePlayer1, fakePlayer2)

      playersGroup = createGroup(fakePlayer1.uuid, fakePlayer2.uuid)!!

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
