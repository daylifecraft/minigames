package com.daylifecraft.minigames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.daylifecraft.minigames.fakeplayer.FakePlayer;
import com.daylifecraft.minigames.fakeplayer.FakePlayerOption;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;

public class UtilsForTesting {

  private static final FakePlayerOption DEFAULT_FAKE_PLAYER_OPTIONS =
    new FakePlayerOption().setRegistered(true).setInTabList(true);

  private static final long DEFAULT_WAITING_TIMEOUT_MS = 250;

  private static final Map<UUID, Instance> spawnInstancesMap = new HashMap<>();

  private static final List<UUID> spawnedPlayers = new ArrayList<>();

  public static void loadOnStartup() {
    var listener =
      EventListener.builder(PlayerSpawnEvent.class)
        .handler(event -> spawnedPlayers.add(event.getPlayer().getUuid()))
        .build();
    EventNode<PlayerEvent> eventNode = EventNode.type("spawn-listener", EventFilter.PLAYER);
    eventNode.setPriority(Integer.MAX_VALUE);
    eventNode.addListener(listener);

    MinecraftServer.getGlobalEventHandler().addChild(eventNode);
  }

  public static Player initFakePlayer(
    final UUID uuid,
    final String playerName,
    final FakePlayerOption options,
    final Consumer<FakePlayer> onSpawnCallback) {
    FakePlayer.initPlayer(uuid, playerName, options, onSpawnCallback);

    return (Player) Entity.getEntity(uuid);
  }

  public static Player initFakePlayer(
    final UUID uuid,
    final String playerName,
    final FakePlayerOption options,
    final Instance spawnInstance) {
    spawnInstancesMap.put(uuid, spawnInstance);
    return initFakePlayer(
      uuid,
      playerName,
      options,
      fakePlayer -> {
        if (fakePlayer.getInstance() != spawnInstance) {
          fakePlayer.setInstance(spawnInstance);
        }
      });
  }

  public static Player initFakePlayer(
    final UUID uuid, final String playerName, final Instance spawnInstance) {
    return initFakePlayer(uuid, playerName, DEFAULT_FAKE_PLAYER_OPTIONS, spawnInstance);
  }

  public static Player initFakePlayer(
    final UUID uuid, final String playerName, final FakePlayerOption options) {
    return initFakePlayer(uuid, playerName, options, fakePlayer -> {
    });
  }

  public static Player initFakePlayer(final UUID uuid, final String playerName) {
    return initFakePlayer(uuid, playerName, DEFAULT_FAKE_PLAYER_OPTIONS, fakePlayer -> {
    });
  }

  public static Player initFakePlayer(final String playerName) {
    return initFakePlayer(PlayerManager.getPlayerUuid(playerName), playerName);
  }

  public static Player initFakePlayer(final String playerName, final FakePlayerOption options) {
    return initFakePlayer(PlayerManager.getPlayerUuid(playerName), playerName, options);
  }

  public static Player initFakePlayer(
    final String playerName, final FakePlayerOption options, final Instance instance) {
    return initFakePlayer(PlayerManager.getPlayerUuid(playerName), playerName, options, instance);
  }

  public static Player initFakePlayer(final String playerName, final Instance instance) {
    return initFakePlayer(PlayerManager.getPlayerUuid(playerName), playerName, instance);
  }

  public static Player initFakePlayer(
    final String playerName,
    final FakePlayerOption options,
    final Consumer<FakePlayer> onSpawnCallback) {
    return initFakePlayer(
      PlayerManager.getPlayerUuid(playerName), playerName, options, onSpawnCallback);
  }

  public static void waitUntilPlayerJoin(final Player player) throws InterruptedException {
    final Instance joinInstance = spawnInstancesMap.getOrDefault(player.getUuid(), null);
    if (joinInstance == null) {
      while (player.getInstance() == null) {
        Thread.sleep(DEFAULT_WAITING_TIMEOUT_MS);
      }
    } else {
      while (player.getInstance() != joinInstance) {
        Thread.sleep(DEFAULT_WAITING_TIMEOUT_MS);
      }
    }

    while (!isPlayerSpawned(player)) {
      Thread.sleep(DEFAULT_WAITING_TIMEOUT_MS);
    }

    spawnInstancesMap.remove(player.getUuid());
  }

  public static void waitUntilPlayerJoin(final Player... players) throws InterruptedException {
    for (Player player : players) {
      waitUntilPlayerJoin(player);
    }
  }

  public static void waitUntilNewInstance(final Player player, final Instance ExpectedInstance)
    throws InterruptedException {
    while (!player.getInstance().equals(ExpectedInstance)) {
      Thread.sleep(DEFAULT_WAITING_TIMEOUT_MS);
    }

    while (!isPlayerSpawned(player)) {
      Thread.sleep(DEFAULT_WAITING_TIMEOUT_MS);
    }
  }

  private static boolean isPlayerSpawned(Player player) {
    var result = spawnedPlayers.contains(player.getUuid());
    spawnedPlayers.remove(player.getUuid());

    return result;
  }
}
