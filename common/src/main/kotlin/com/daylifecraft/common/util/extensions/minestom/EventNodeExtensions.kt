package com.daylifecraft.common.util.extensions.minestom

import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import java.util.function.Consumer

/**
 * Adds listener to the event node.
 *
 * @param listener the listener to add to node.
 * @param T type of event to listen. Note that this type could be inferred by lambda argument type.
 */
inline fun <reified E : T, reified T : Event> EventNode<T>.addListener(listener: Consumer<E>) =
  addListener(E::class.java, listener)
