package com.daylifecraft.minigames.text.i18n

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class LangTest {

  @Test
  fun testFallback() {
    // The String `debug.debug-string-welcome` is translated in ru_ru and in en_us
    // So when we're trying to get it from not existed language `mega-unknown-lang-code`
    // we must get string from fallback lang(en_us).
    // Also checking that this string in ru_ru is different from en_us

    val stringFromUnknownLang = Lang.string("mega-unknown-lang-code", "debug.debug-string-welcome")
    val stringFromDefaultLang = Lang.string(Lang.DEFAULT_LANGUAGE, "debug.debug-string-welcome")
    assertEquals(stringFromDefaultLang, stringFromUnknownLang, "Unknown lang must fallback to default lang")

    val stringFromRussianLang = Lang.string("ru_ru", "debug.debug-string-welcome")
    assertNotEquals(stringFromDefaultLang, stringFromRussianLang, "Check that russian isn't default language")
  }
}
