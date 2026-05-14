package com.example.a90phase.presentation.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.a90phase.presentation.theme.BackgroundGradient
import com.example.a90phase.presentation.theme.SleepColors

// Reads isOnboardingCompleted — wire to ViewModel in a future ticket.
// Currently always routes to onboarding.
@Suppress("UnusedParameter")
@Composable
fun SplashScreen(
    onOnboardingNeeded: () -> Unit,
    onGoToMain: () -> Unit,
) {
    LaunchedEffect(Unit) { onOnboardingNeeded() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradient),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = SleepColors.CyanGlow)
    }
}
