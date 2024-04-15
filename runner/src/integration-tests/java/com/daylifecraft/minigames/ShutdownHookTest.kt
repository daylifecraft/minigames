package com.daylifecraft.minigames;

import com.daylifecraft.minigames.event.server.ServerStopEvent;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ShutdownHookTest {

  @Test
  void test() {
    final var eventCalled = new AtomicBoolean(false);
    MinecraftServer.getGlobalEventHandler()
      .addListener(ServerStopEvent.class, serverStopEvent -> eventCalled.set(true));
    final ShutdownHook shutdownHook = Init.getShutdownHook();
    shutdownHook.run(1);

    Assertions.assertTrue(eventCalled.get(), "ServerStopEvent should be called");
  }
}
