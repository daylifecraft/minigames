package com.daylifecraft.minigames.player

import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.UtilsForTesting
import com.daylifecraft.minigames.exception.InvalidPlayerUsername
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

internal class GeneralPlayerUUIDTest {
  private val playerUuid: UUID = UUID.fromString("49e96464-aa17-46b3-b5c6-87574b6b48b4")

  @Test
  fun testPlayerUUID() {
    assertEquals(
      expected = UUID.nameUUIDFromBytes("Bot3".toByteArray(StandardCharsets.UTF_8)),
      actual = PlayerManager.getPlayerUuid("Bot3"),
      message = "assert that UUID of a player is the same",
    )
  }

  @Test
  fun testIncorrectPlayerUUID() {
    assertFailsWith<InvalidPlayerUsername>(message = "Player UUID should be returned only for correct nicknames") {
      PlayerManager.getPlayerUuid("A")
    }
  }

  @Test
  fun testRegisteredPlayerUUID() {
    val fakePlayer = UtilsForTesting.initFakePlayer("Bot", playerUuid)
    assertNotNull(PlayerManager.getPlayerUuid("Bot"), "assert that we get the UUID")

    fakePlayer.kick("")
  }
}
