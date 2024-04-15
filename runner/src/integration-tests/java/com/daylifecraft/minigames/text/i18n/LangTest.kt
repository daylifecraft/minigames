package com.daylifecraft.minigames.text.i18n;

import com.daylifecraft.minigames.Init;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class LangTest {
  @BeforeAll
  static void setupLang() throws IOException {
    Init.initLang();
  }

  @Test
  void testFallback() {
    // The String `debug.debug-string-welcome` is translated in ru_ru and in en_us
    // So when we're trying to get it from not existed language `mega-unknown-lang-code`
    // we must get string from fallback lang(en_us).
    // Also checking that this string in ru_ru is different from en_us

    final String unk = Lang.string("mega-unknown-lang-code", "debug.debug-string-welcome");
    final String def = Lang.string(Lang.DEFAULT_LANGUAGE, "debug.debug-string-welcome");
    Assertions.assertEquals(def, unk, "Value from def & eng need equals");

    final String rus = Lang.string("ru_ru", "debug.debug-string-welcome");
    Assertions.assertNotEquals(def, rus, "Def & Rus need not equals!");
  }
}
