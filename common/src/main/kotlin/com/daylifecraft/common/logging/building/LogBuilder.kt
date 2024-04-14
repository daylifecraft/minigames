package com.daylifecraft.common.logging.building

import com.daylifecraft.common.logging.foundation.Level
import net.minestom.server.entity.Player
import java.util.UUID

interface LogBuilder {
  /** Set player to this builder  */
  fun player(player: Player) = player(player.uuid)

  /** Set player to this builder  */
  fun player(player: UUID)

  /** Set log level.  */
  fun level(level: Level)

  /** Set message  */
  fun message(s: String?)

  /** Append details  */
  fun details(key: String, value: Any?)

  /** Edit section details.  */
  fun detailsSection(section: String, key: String, value: Any?)

  /** Complete and send log.  */
  fun complete()

  companion object {
    /** Default details section name for additional data. Used with [detailsSection]  */
    const val KEY_ADDITIONAL: String = "additionalInformation"
  }
}
