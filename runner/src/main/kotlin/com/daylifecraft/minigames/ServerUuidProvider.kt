package com.daylifecraft.minigames

import java.util.UUID

object ServerUuidProvider {
  val uuid: UUID by lazy {
    UUID.randomUUID()
  }
}
