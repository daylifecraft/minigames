package com.daylifecraft.minigames.instance

import net.minestom.server.instance.InstanceManager

interface InstanceController {
  val manager: CraftInstancesManager

  val defaultManager: InstanceManager

  fun detached(craftInstance: AbstractCraftInstance?)

  fun attached(craftInstance: AbstractCraftInstance?)
}
