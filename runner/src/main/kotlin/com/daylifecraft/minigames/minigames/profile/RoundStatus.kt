package com.daylifecraft.minigames.minigames.profile

/**
 * Enum represent Round status to simply work with it. Ever enum have string representation to store
 * in database
 */
enum class RoundStatus(val stringRepresentation: String, val isStopState: Boolean) {
  INITIALIZING("initializing", false),
  PREPARING("preparing", false),
  STARTED("started", false),
  ENDED("ended", true),
  CANCELLED("cancelled", true),
  ;

  companion object {
    /**
     * Get RoundStatus by string
     *
     * @param stringRepresentation string representation (used in database)
     * @return RoundStatus enum object
     */
    @JvmStatic
    fun getByStringRepresentation(stringRepresentation: String): RoundStatus? {
      for (roundStatus in entries) {
        if (roundStatus.stringRepresentation == stringRepresentation) {
          return roundStatus
        }
      }

      return null
    }
  }
}
