package com.example.a90phase.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title.uppercase(),
        style = SleepTypography.LabelMedium,
        color = SleepColors.SlateBlue,
        modifier = modifier.padding(
            horizontal = Spacing.Medium,
            vertical = Spacing.XS,
        ),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun SectionHeaderPreview() {
    NightSkyTheme {
        SectionHeader(title = "Rekommenderade sänggångstider")
    }
}
