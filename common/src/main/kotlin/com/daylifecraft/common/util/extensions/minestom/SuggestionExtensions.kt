package com.daylifecraft.common.util.extensions.minestom

import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionEntry

/**
 * Gets substring of currently entered string, starting at [Suggestion.start]
 */
val Suggestion.currentArgumentValue: String
  get() = input.substring(start - 1).trim { it <= ' ' }

/**
 * Adds suggestion entries from iterable
 *
 * @param entries entries to add.
 */
fun Suggestion.addEntries(entries: Iterable<SuggestionEntry>) =
  entries.forEach { addEntry(it) }

/**
 * Adds suggestion entries from sequence
 *
 * @param entries entries to add.
 */
fun Suggestion.addEntries(entries: Sequence<SuggestionEntry>) =
  entries.forEach { addEntry(it) }

/**
 * Adds suggestion entries from iterable.
 *
 * Applies [transform] function to each element from [entries] to map them to [SuggestionEntry]
 * Filtering could be also done using transform function by mapping [T] to null.
 *
 * @param entries entries to add.
 */
inline fun <T> Suggestion.addEntries(entries: Iterable<T>, transform: (T) -> SuggestionEntry?) {
  for (entry in entries) {
    transform(entry)?.let { addEntry(it) }
  }
}

/**
 * Adds suggestion entries from sequence.
 *
 * Applies [transform] function to each element from [entries] to map them to [SuggestionEntry]
 * Filtering could be also done using transform function by mapping [T] to null.
 *
 * @param entries entries to add.
 */
inline fun <T> Suggestion.addEntries(entries: Sequence<T>, transform: (T) -> SuggestionEntry?) {
  for (entry in entries) {
    transform(entry)?.let { addEntry(it) }
  }
}

/**
 * Creates suggestion entry out of string.
 *
 * @return Suggestion entry with received string if it's not null, or null otherwise.
 */
fun String?.toSuggestionEntry(): SuggestionEntry? =
  this?.let { SuggestionEntry(it) }
