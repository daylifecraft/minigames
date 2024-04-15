package com.daylifecraft.minigames

import java.util.UUID

object ServerUuidProvider {

  /**
   * Server uuid
   * A random uuid which is generated lazily.
   * It's thread safe
   */
  val uuid: UUID by lazy {
    UUID.randomUUID()
  }
}
