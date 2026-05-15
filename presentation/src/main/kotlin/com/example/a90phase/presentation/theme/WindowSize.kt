package com.example.a90phase.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun rememberIsCompactHeight(): Boolean = LocalConfiguration.current.screenHeightDp < 480
