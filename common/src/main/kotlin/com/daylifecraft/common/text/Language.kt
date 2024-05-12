package com.daylifecraft.common.text

import java.io.File

class Language {
  private val loadedKeys = mapOf<String, String>()

  fun getByKey(key: String) = loadedKeys[key]
}

class LanguagesCollection {
  private val languages = mapOf<String, Language>()

  val defaultLanguage: String
    get() = "en_us"

  fun getLang(languageCode: String) = languages[languageCode]
  fun getTranslatable(key: String) = Translatable(key, this)
}

class Translatable(
  val key: String,
  private val collection: LanguagesCollection,
) {
  // Fallback should work
  fun getIn(languageCode: String) {

    collection.getLang(languageCode)?.getByKey(key)
  }
}


fun loadLanguageFromFile(file: File): List<Language> {
  TODO(file.toString())
}

fun loadLanguagesCollectionFromFile(file: File): LanguagesCollection {
  TODO(file.toString())
}
