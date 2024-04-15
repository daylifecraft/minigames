package com.daylifecraft.minigames.instance

import io.mockk.every
import io.mockk.mockk
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.event.Event
import net.minestom.server.event.trait.EntityEvent
import net.minestom.server.event.trait.InstanceEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.world.DimensionType
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class InstanceUtilTest {
  @Test
  fun testIsEventRelated() {
    val instance = mockk<Instance>(relaxed = true)
    every { instance.isRegistered } returns true

    val randomEvent = object : Event {}
    assertFalse(
      InstanceUtil.isEventRelated(randomEvent, instance),
      message = "Random event should not be related to any instance",
    )

    val randomInstanceEvent = InstanceEvent { InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD) }
    assertFalse(
      InstanceUtil.isEventRelated(randomInstanceEvent, instance),
      message = "InstanceEvent should not be related to the instance",
    )

    val randomEntityEvent = EntityEvent { Entity(EntityType.ALLAY) }
    assertFalse(
      InstanceUtil.isEventRelated(randomEntityEvent, instance),
      message = "EntityEvent should not be related to the instance",
    )

    val currentInstanceEvent = InstanceEvent { instance }
    assertTrue(
      InstanceUtil.isEventRelated(currentInstanceEvent, instance),
      message = "InstanceEvent should be related to the instance",
    )

    val currentInstanceEntityEvent = EntityEvent {
      Entity(EntityType.ALLAY).apply { setInstance(instance) }
    }
    assertTrue(
      InstanceUtil.isEventRelated(currentInstanceEntityEvent, instance),
      message = "EntityEvent should be related to the instance",
    )
  }

  @Test
  fun testIsEntityInInstance() {
    val instance = mockk<Instance>(relaxed = true)
    every { instance.isRegistered } returns true

    val entity = Entity(EntityType.ALLAY)
    assertFalse(
      InstanceUtil.isEntityInInstance(entity, instance),
      message = "Entity should not be in the instance",
    )

    val entityT = Entity(EntityType.ALLAY).apply { setInstance(instance) }
    assertTrue(
      InstanceUtil.isEntityInInstance(entityT, instance),
      message = "Entity should be in the instance",
    )
  }
}
