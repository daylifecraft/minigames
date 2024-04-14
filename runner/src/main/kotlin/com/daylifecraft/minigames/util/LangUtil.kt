package com.daylifecraft.minigames.util

import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import java.util.regex.Pattern

internal object LangUtil {
  // "$(translate:".length
  private const val TRANSLATE_PREFIX_LENGTH = 12

  // Matches strings like $(translate:some-translatable.variable)
  private val TRANSLATE_VARIABLE_PATTERN: Pattern = Pattern.compile("\\$\\(translate:[a-zA-Z:0-9_.-]*\\)")

  /**
   * Replace translation keys with translation
   *
   * @param playerLanguage language
   * @param source the string in which we need to replace the variables and translation keys
   * @param variables the variables we will replace with
   * @return translated string with replaced values
   */
  @JvmStatic
  fun replaceAllTranslateKeys(
    playerLanguage: PlayerLanguage,
    source: String,
    vararg variables: Pair<String, String?>,
  ): String {
    // TODO Check is this method could handle deep nesting. If not then make this and calling method recursive.
    if (!source.contains("$(translate:")) {
      return source
    }

    val translateVars =
      TRANSLATE_VARIABLE_PATTERN.matcher(source).results().map {
        source.substring(it.start(), it.end())
      }

    var result = source

    for (translateVar in translateVars) {
      result =
        result.replace(
          translateVar,
          playerLanguage.string(
            getTranslatableKey(translateVar),
            *variables,
          ),
        )
    }
    return result
  }

  /**
   * Used to remove "$(translate:" and ")"
   * Example: "$(translate:some-translatable.variable)" -> "some-translatable.variable"
   */
  private fun getTranslatableKey(input: String): String = input.substring(TRANSLATE_PREFIX_LENGTH, input.length - 1)
}
