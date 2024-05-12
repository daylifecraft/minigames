package com.daylifecraft.common.config

import java.util.LinkedList

@JvmInline
value class ConfigPath(val tokens: LinkedList<PathToken> = LinkedList<PathToken>()) {

  val size
    get() = tokens.size

  fun first() = tokens.first()
  fun last() = tokens.last()

  fun addFirst(token: PathToken) = tokens.addFirst(token)
  fun addLast(token: PathToken) = tokens.addLast(token)

  fun removeFirst(): PathToken = tokens.removeFirst()
  fun removeLast(): PathToken = tokens.removeLast()

  override fun toString(): String = tokens.joinToString(".") {
    when (it) {
      is StringToken -> it.value
      is IntToken -> "[${it.value}]"
    }
  }
}

sealed interface PathToken

@JvmInline
value class StringToken(val value: String) : PathToken

@JvmInline
value class IntToken(val value: Int) : PathToken
