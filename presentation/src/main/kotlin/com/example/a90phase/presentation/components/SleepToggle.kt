package com.example.a90phase.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography

@Composable
fun SleepToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = SleepTypography.BodyLarge,
            color = SleepColors.White,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = SleepColors.CyanGlow,
                uncheckedTrackColor = SleepColors.MidnightBlue,
                checkedThumbColor = SleepColors.DeepSpace,
                uncheckedThumbColor = SleepColors.Silver,
            ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun SleepToggleOnPreview() {
    NightSkyTheme {
        SleepToggle(label = "Daily Check-in (18:00)", checked = true, onCheckedChange = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun SleepToggleOffPreview() {
    NightSkyTheme {
        SleepToggle(label = "Smart Wake Window", checked = false, onCheckedChange = {})
    }
}
