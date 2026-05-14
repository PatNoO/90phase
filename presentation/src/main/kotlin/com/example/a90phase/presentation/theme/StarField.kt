package com.example.a90phase.presentation.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private data class Star(val x: Float, val y: Float, val radius: Dp, val alpha: Float)

private fun generateStars(count: Int, seed: Int): List<Star> {
    val rng = Random(seed)
    val sizes = listOf(1.5.dp, 2.5.dp, 3.5.dp)
    return List(count) {
        Star(
            x = rng.nextFloat(),
            y = rng.nextFloat(),
            radius = sizes.random(rng),
            alpha = rng.nextFloat() * 0.4f + 0.15f,
        )
    }
}

@Composable
fun StarFieldBackground(modifier: Modifier = Modifier) {
    val stars = remember { generateStars(count = 40, seed = 90) }
    Canvas(modifier = modifier.fillMaxSize()) {
        stars.forEach { star ->
            drawCircle(
                color = Color.White.copy(alpha = star.alpha),
                radius = star.radius.toPx(),
                center = Offset(size.width * star.x, size.height * star.y),
            )
        }
    }
}
