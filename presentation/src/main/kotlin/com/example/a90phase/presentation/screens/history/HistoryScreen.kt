package com.example.a90phase.presentation.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.a90phase.presentation.theme.BackgroundGradient
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography

// Stub — full implementation in PH-18
@Suppress("UnusedParameter")
@Composable
fun HistoryScreen(onNavigateToLogDetail: (logId: String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradient),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Sömnhistorik",
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
        )
    }
}
