package com.daylifecraft.minigames.player;

import com.daylifecraft.minigames.PlayerManager;
import com.daylifecraft.minigames.UtilsForTesting;
import com.daylifecraft.minigames.exception.InvalidPlayerUsername;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GeneralPlayerUUIDTest {
  private final UUID playerUuid = UUID.fromString("49e96464-aa17-46b3-b5c6-87574b6b48b4");

  @Test
  void testPlayerUUID() {
    Assertions.assertEquals(
      UUID.nameUUIDFromBytes(("Bot3").getBytes(StandardCharsets.UTF_8)),
      PlayerManager.getPlayerUuid("Bot3"),
      "assert that UUID of a player is the same");
  }

  @Test
  void testIncorrectPlayerUUID() {
    Assertions.assertThrows(
      InvalidPlayerUsername.class,
      () -> PlayerManager.getPlayerUuid("A"),
      "assert that we don't get UUID");
  }

  @Test
  void testRegisteredPlayerUUID() {
    UtilsForTesting.initFakePlayer(playerUuid, "Bot");
    Assertions.assertNotNull(PlayerManager.getPlayerUuid("Bot"), "assert that we get the UUID");

    ((Player) Entity.getEntity(playerUuid)).kick("");
  }
}
