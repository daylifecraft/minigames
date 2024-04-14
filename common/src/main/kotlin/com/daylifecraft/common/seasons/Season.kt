package com.daylifecraft.common.seasons

import com.daylifecraft.common.seasons.SeasonDate.Companion.current

/** Represents a season with name, display name, start and end date.  */
class Season
@JvmOverloads
constructor(
  @JvmField val name: String,
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
    get() =
      when (activeness) {
        Activeness.FORCE_ACTIVE -> true
        Activeness.FORCE_STOPPED -> false
        Activeness.INITIAL -> isInitiallyActive
      }

  /**
   * Creates season from map with properties. And calculates is it active or not.
   *
   * @param valuesMap map, that contains 'name', 'display_name', 'start_date', 'end_date' values.
   */
  constructor(valuesMap: Map<String, Any?>, priority: Int) : this(
    valuesMap["name"] as String,
    valuesMap["display_name"] as String,
    SeasonDate(valuesMap["start_date"] as String),
    SeasonDate(valuesMap["end_date"] as String),
    priority,
  )

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
