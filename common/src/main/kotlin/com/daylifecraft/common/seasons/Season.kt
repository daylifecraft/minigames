package com.daylifecraft.common.seasons

import com.daylifecraft.common.seasons.SeasonDate.Companion.current

/**
 * Represents a season with name, display name, start and end date.
 *
 * @property name unique name of the season
 * @property displayName i18n key
 * @property startDate season start date inclusive
 * @property endDate season end date also inclusive
 */
class Season(
  val name: String,
  val displayName: String,
  val startDate: SeasonDate,
  val endDate: SeasonDate,
  private val initialPriority: Int = 0,
) {
  var attachedList: SeasonsList? = null
  var priority: Int = initialPriority
    set(value) {
      checkNotNull(attachedList) { "Season should be attached to any list." }

      field = value
      attachedList!!.dropCaches()
    }

  private val isInitiallyActive = current().isBetween(startDate, endDate)
  private var activeness = Activeness.INITIAL

  /** Returns true when season was force active, or now it's time.  */
  val isActive: Boolean
    get() = when (activeness) {
      Activeness.FORCE_ACTIVE -> true
      Activeness.FORCE_STOPPED -> false
      Activeness.INITIAL -> isInitiallyActive
    }

  /** Sets activeness, resets priority and drops attached list caches.  */
  fun setActiveness(activeness: Activeness) {
    checkNotNull(attachedList) { "Season should be attached to any list." }

    priority = initialPriority
    this.activeness = activeness
    attachedList!!.dropCaches()
  }

  /** Describe strategy for isActive calculation.  */
  enum class Activeness {
    FORCE_ACTIVE,
    FORCE_STOPPED,
    INITIAL,
  }
}
