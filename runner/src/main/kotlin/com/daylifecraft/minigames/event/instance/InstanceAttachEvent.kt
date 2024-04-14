package com.daylifecraft.minigames.event.instance

import com.daylifecraft.minigames.instance.AbstractCraftInstance
import com.daylifecraft.minigames.instance.CraftInstancesManager
import net.minestom.server.event.Event

data class InstanceAttachEvent(
  val instance: AbstractCraftInstance,
  val instancesManager: CraftInstancesManager,
) : Event
