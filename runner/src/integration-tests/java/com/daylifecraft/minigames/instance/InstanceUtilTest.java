package com.daylifecraft.minigames.instance;

import java.util.UUID;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.world.DimensionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InstanceUtilTest {
  @Test
  void testIsEventRelated() {
    final InstanceManager instanceManager = MinecraftServer.getInstanceManager();
    final Instance instance = instanceManager.createInstanceContainer();

    // == random event == (false)
    // this event not contains instance => always false
    final Event randomEvent = new Event() {
    };
    Assertions.assertFalse(
      InstanceUtil.isEventRelated(randomEvent, instance),
      "Random event should not be related to any instance");

    // == no related == (false)
    // these events contain instance, but not the one
    final Event noRelatedInstanceEvent =
      (InstanceEvent) () -> new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
    Assertions.assertFalse(
      InstanceUtil.isEventRelated(noRelatedInstanceEvent, instance),
      "InstanceEvent should not be related to the instance");

    final Event noRelatedEntityEvent = (EntityEvent) () -> new Entity(EntityType.ALLAY);
    Assertions.assertFalse(
      InstanceUtil.isEventRelated(noRelatedEntityEvent, instance),
      "EntityEvent should not be related to the instance");

    // == related == (true)
    // these events contain the desired instance
    final Event relatedInstanceEvent = (InstanceEvent) () -> instance;
    Assertions.assertTrue(
      InstanceUtil.isEventRelated(relatedInstanceEvent, instance),
      "InstanceEvent should be related to the instance");

    final Event relatedEntityEvent =
      (EntityEvent)
        () -> {
          final Entity entity = new Entity(EntityType.ALLAY);
          entity.setInstance(instance);
          return entity;
        };
    Assertions.assertTrue(
      InstanceUtil.isEventRelated(relatedEntityEvent, instance),
      "EntityEvent should be related to the instance");
  }

  @Test
  void testIsEntityInInstance() {
    final InstanceManager instanceManager = MinecraftServer.getInstanceManager();
    final Instance instance = instanceManager.createInstanceContainer();

    final Entity entity = new Entity(EntityType.ALLAY);
    Assertions.assertFalse(
      InstanceUtil.isEntityInInstance(entity, instance), "Entity should not be in the instance");

    final Entity entityT = new Entity(EntityType.ALLAY);
    entityT.setInstance(instance);

    Assertions.assertTrue(
      InstanceUtil.isEntityInInstance(entityT, instance), "Entity should be in the instance");
  }
}
