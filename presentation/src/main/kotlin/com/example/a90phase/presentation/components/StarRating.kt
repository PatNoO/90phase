package com.example.a90phase.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.example.a90phase.presentation.R
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography

private const val MAX_STARS = 5
private const val STAR_FILLED = "★"
private const val STAR_EMPTY = "☆"

@Composable
fun StarRating(
    rating: Int?,
    modifier: Modifier = Modifier,
) {
    val displayRating = rating?.coerceIn(0, MAX_STARS) ?: 0
    val description = stringResource(R.string.star_rating_description, displayRating, MAX_STARS)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = description
        },
    ) {
        repeat(MAX_STARS) { index ->
            if (index < displayRating) {
                Text(
                    text = STAR_FILLED,
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.GoodAmber,
                )
            } else {
                Text(
                    text = STAR_EMPTY,
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.SlateBlue,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun StarRatingFullPreview() {
    NightSkyTheme {
        StarRating(rating = 5)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun StarRatingPartialPreview() {
    NightSkyTheme {
        StarRating(rating = 3)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun StarRatingNullPreview() {
    NightSkyTheme {
        StarRating(rating = null)
    }
}
