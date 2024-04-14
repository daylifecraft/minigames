package com.daylifecraft.minigames;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GeneralServerUUIDTest {
  private static UUID serverUuid;

  @BeforeAll
  static void ServerUUIDInitializationCheck() {
    serverUuid = Init.getUUID();
    Assertions.assertNotNull(serverUuid, "assert that UUID of a server is not null");
  }

  @Test
  void testServerUUIDOverwrite() {
    Init.initServerUuid();
    Assertions.assertEquals(
      Init.getUUID(), serverUuid, "assert that the old and the new UUIDs of a server are equal ");
  }
}
