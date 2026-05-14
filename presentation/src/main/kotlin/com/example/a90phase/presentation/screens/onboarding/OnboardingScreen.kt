package com.example.a90phase.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.OnboardingBackgroundGradient
import com.example.a90phase.presentation.theme.OnboardingNebulaWash
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing

private const val ONBOARDING_PAGE_COUNT = 8

private val onboardingPageTitles = listOf(
    "Välkommen",
    "Behörigheter",
    "Vaknatid",
    "Daglig check-in",
    "Sänggångspåminnelse",
    "Morgon check-in",
    "Smart Wake Window",
    "Discovery Phase",
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = OnboardingBackgroundGradient),
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush = OnboardingNebulaWash))
        Column(modifier = Modifier.fillMaxSize()) {
            OnboardingTopBar(currentPage = pagerState.currentPage)
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                OnboardingPageContent(page = page)
            }
            OnboardingBottomBar(pagerState = pagerState, onComplete = onComplete)
        }
    }
}

@Composable
private fun OnboardingTopBar(currentPage: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Small, vertical = Spacing.XS),
    ) {
        if (currentPage > 0) {
            TextButton(onClick = {}) {
                Text(
                    text = "←",
                    style = SleepTypography.HeadlineMedium,
                    color = SleepColors.Silver,
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: Int) {
    // Stub — replace with real page composables in PH-20
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = onboardingPageTitles[page],
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
        )
    }
}

@Composable
private fun OnboardingBottomBar(pagerState: PagerState, onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PageIndicator(
            pageCount = ONBOARDING_PAGE_COUNT,
            currentPage = pagerState.currentPage,
        )
        Spacer(modifier = Modifier.height(Spacing.Large))
        if (pagerState.currentPage == ONBOARDING_PAGE_COUNT - 1) {
            TextButton(onClick = onComplete) {
                Text(
                    text = "Kom igång",
                    style = SleepTypography.BodyLarge,
                    color = SleepColors.CyanGlow,
                )
            }
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (isActive) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) SleepColors.CyanGlow
                        else SleepColors.White.copy(alpha = 0.3f),
                    ),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040)
@Composable
internal fun OnboardingScreenPreview() {
    NightSkyTheme {
        OnboardingScreen(onComplete = {})
    }
}
