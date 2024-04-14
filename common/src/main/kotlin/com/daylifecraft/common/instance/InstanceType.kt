package com.daylifecraft.common.instance

enum class InstanceType(
  /** Checks whether the server needs to create an instance of this type when the server starts.  */
  val isCreatedOnStartup: Boolean,
) {
  LOBBY(true),
  MINI_GAME(false),
}
