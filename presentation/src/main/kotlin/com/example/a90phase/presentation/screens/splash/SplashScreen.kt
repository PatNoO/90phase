package com.example.a90phase.presentation.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.a90phase.presentation.theme.BackgroundGradient
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.viewmodels.SplashViewModel

@Composable
fun SplashScreen(
    onOnboardingNeeded: () -> Unit,
    onGoToMain: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val isCompleted by viewModel.isOnboardingCompleted.collectAsState()

    LaunchedEffect(isCompleted) {
        when (isCompleted) {
            true -> onGoToMain()
            false -> onOnboardingNeeded()
            null -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradient),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = SleepColors.CyanGlow)
    }
}
