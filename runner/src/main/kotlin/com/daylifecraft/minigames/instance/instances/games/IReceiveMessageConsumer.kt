package com.daylifecraft.minigames.instance.instances.games

import net.minestom.server.entity.Player

/** Consumer for check, can the player receive message from  */
fun interface IReceiveMessageConsumer {
  /**
   * Check, can @recipient receive message from @sender
   *
   * @param recipient Player instance
   * @param sender Sender player instance
   * @return True, if player can. Otherwise False
   */
  fun canReceiveMessageFrom(recipient: Player, sender: Player): Boolean
}
