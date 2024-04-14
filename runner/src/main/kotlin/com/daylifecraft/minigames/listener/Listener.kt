package com.daylifecraft.minigames.listener

import net.minestom.server.event.Event

abstract class Listener<E : Event?> protected constructor(val eventClass: Class<E>) {
  protected abstract fun onCalled(event: E)

  /** The method will be called when the event is processed  */
  fun onCalled(event: Any?) {
    onCalled(eventClass.cast(event))
  }
}
