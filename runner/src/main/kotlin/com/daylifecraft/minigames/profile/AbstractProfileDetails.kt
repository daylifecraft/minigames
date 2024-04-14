package com.daylifecraft.minigames.profile

import com.daylifecraft.common.util.extensions.miniMessage
import com.daylifecraft.minigames.text.i18n.Language
import net.kyori.adventure.text.Component
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

abstract class AbstractProfileDetails protected constructor(
  @JvmField protected val profile: AbstractProfile,
  @JvmField var language: Language,
) {
  /** Show profile details  */
  fun show(): Component =
    text.miniMessage()

  protected abstract val text: String

  companion object {
    @JvmStatic
    protected fun parseTime(time: String?): String = try {
      SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        .format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH).parse(time))
    } catch (e: ParseException) {
      // TODO Could not parse time
      "Could not parse time"
    }
  }
}
