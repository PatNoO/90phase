package com.example.a90phase.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.example.a90phase.presentation.R
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Full date with weekday, e.g. "Thursday 15 May 2025" / "torsdag 15 maj 2025". */
const val FULL_DATE_PATTERN = "EEEE d MMMM yyyy"

/** Compact date for list rows, e.g. "Thursday  15 May" / "torsdag  15 maj". */
const val CARD_DATE_PATTERN = "EEEE  d MMM"

/**
 * The locale Android actually resolved the app's strings to, read from a resource rather than
 * [Locale.getDefault].
 *
 * The app ships `values/` (English) and `values-sv/` (Swedish). A device set to any third
 * language falls back to the English strings, and must get English month and weekday names to
 * go with them — `Locale.getDefault()` would hand back that third language and produce a screen
 * mixing two languages.
 */
@Composable
fun appLocale(): Locale = Locale.forLanguageTag(stringResource(R.string.locale_tag))

/**
 * A [DateTimeFormatter] for [pattern] in the app's resolved locale.
 *
 * Note that casing is the locale's business, not ours: `EEEE` yields "Thursday" in English and
 * "torsdag" in Swedish, and both are correct for their language. Do not capitalise the result.
 */
@Composable
fun rememberDateFormatter(pattern: String): DateTimeFormatter {
    val locale = appLocale()
    return remember(pattern, locale) { DateTimeFormatter.ofPattern(pattern, locale) }
}

/** The weekday name in the app's resolved locale — "Thursday" in English, "torsdag" in Swedish. */
@Composable
fun DayOfWeek.localizedName(): String = getDisplayName(TextStyle.FULL_STANDALONE, appLocale())
