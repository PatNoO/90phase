package com.example.a90phase.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography

private val ButtonHeight = 56.dp
private val ButtonShape = RoundedCornerShape(12.dp)

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = SleepColors.CyanGlow,
            contentColor = SleepColors.DeepSpace,
            disabledContainerColor = SleepColors.CyanGlow.copy(alpha = 0.38f),
            disabledContentColor = SleepColors.DeepSpace.copy(alpha = 0.38f),
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight),
    ) {
        Text(text = text, style = SleepTypography.BodyLarge)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = SleepColors.DeepSpace.copy(alpha = 0f),
            contentColor = SleepColors.CyanGlow,
        ),
        border = null,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            .border(
                width = 1.dp,
                color = SleepColors.CyanGlow.copy(alpha = 0.6f),
                shape = ButtonShape,
            ),
    ) {
        Text(text = text, style = SleepTypography.BodyLarge)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun PrimaryButtonPreview() {
    NightSkyTheme {
        PrimaryButton(text = "Set Alarm", onClick = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun SecondaryButtonPreview() {
    NightSkyTheme {
        SecondaryButton(text = "Skip", onClick = {})
    }
}
