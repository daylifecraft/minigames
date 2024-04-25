package com.daylifecraft.minigames.util

class RoundIterator<T>(val data: List<T>) : Iterator<T> {

  private var currentElementIndex = 0

  override fun hasNext(): Boolean = data.isNotEmpty()

  override fun next(): T = data[currentElementIndex++ % data.size]

  fun nextNotNullElement(): T? {
    repeat(data.size) {
      val element = next()

      if (element != null) return element
    }

    return null
  }
}
