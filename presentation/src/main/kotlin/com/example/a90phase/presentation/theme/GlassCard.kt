package com.example.a90phase.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassCard(cornerRadius: Dp = 16.dp): Modifier = this
    .background(
        brush = GlassCardGradient,
        shape = RoundedCornerShape(cornerRadius),
    )
    .border(
        width = 0.5.dp,
        brush = Brush.verticalGradient(
            listOf(
                Color(0x44FFFFFF),
                Color(0x11FFFFFF),
            ),
        ),
        shape = RoundedCornerShape(cornerRadius),
    )
