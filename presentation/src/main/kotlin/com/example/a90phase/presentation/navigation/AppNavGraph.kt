package com.example.a90phase.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.a90phase.presentation.screens.calculator.CalculatorScreen
import com.example.a90phase.presentation.screens.history.HistoryScreen
import com.example.a90phase.presentation.screens.history.LogDetailScreen
import com.example.a90phase.presentation.screens.onboarding.OnboardingScreen
import com.example.a90phase.presentation.screens.settings.SettingsScreen
import com.example.a90phase.presentation.screens.splash.SplashScreen
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography

private val bottomNavRoutes = setOf(Routes.CALCULATOR, Routes.HISTORY)

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SleepColors.NavyBlue,
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                SleepBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navController.navigateToBottomNavTab(route) },
                )
            }
        },
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
    ) {
        authRoutes(navController)
        mainRoutes(navController)
    }
}

private fun NavGraphBuilder.authRoutes(navController: NavHostController) {
    composable(Routes.SPLASH) {
        ScreenFadeIn {
            SplashScreen(
                onOnboardingNeeded = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onGoToMain = {
                    navController.navigate(Routes.CALCULATOR) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }
    }
    composable(Routes.ONBOARDING) {
        ScreenFadeIn {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Routes.CALCULATOR) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }
    }
}

private fun NavGraphBuilder.mainRoutes(navController: NavHostController) {
    composable(Routes.CALCULATOR) {
        ScreenFadeIn {
            CalculatorScreen(onNavigateToSettings = { navController.navigate(Routes.SETTINGS) })
        }
    }
    composable(Routes.HISTORY) {
        ScreenFadeIn {
            HistoryScreen(
                onNavigateToLogDetail = { logId ->
                    navController.navigate(Routes.logDetail(logId))
                },
            )
        }
    }
    composable(Routes.SETTINGS) {
        ScreenFadeIn {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
    composable(
        route = Routes.LOG_DETAIL,
        arguments = listOf(navArgument("logId") { type = NavType.StringType }),
    ) { backStackEntry ->
        val logId = backStackEntry.arguments?.getString("logId").orEmpty()
        ScreenFadeIn {
            LogDetailScreen(logId = logId, onNavigateBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun ScreenFadeIn(content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInHorizontally { it / 4 },
        exit = ExitTransition.None,
    ) {
        content()
    }
}

@Composable
private fun SleepBottomNav(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = SleepColors.MidnightBlue,
        contentColor = SleepColors.CyanGlow,
    ) {
        NavigationBarItem(
            selected = currentRoute == Routes.CALCULATOR,
            onClick = { onNavigate(Routes.CALCULATOR) },
            icon = { Text(text = "◎", style = SleepTypography.HeadlineMedium) },
            label = { Text(text = "Calculator", style = SleepTypography.LabelMedium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SleepColors.CyanGlow,
                selectedTextColor = SleepColors.CyanGlow,
                unselectedIconColor = SleepColors.SlateBlue,
                unselectedTextColor = SleepColors.SlateBlue,
                indicatorColor = SleepColors.GlassSurface,
            ),
        )
        NavigationBarItem(
            selected = currentRoute == Routes.HISTORY,
            onClick = { onNavigate(Routes.HISTORY) },
            icon = { Text(text = "◈", style = SleepTypography.HeadlineMedium) },
            label = { Text(text = "History", style = SleepTypography.LabelMedium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SleepColors.CyanGlow,
                selectedTextColor = SleepColors.CyanGlow,
                unselectedIconColor = SleepColors.SlateBlue,
                unselectedTextColor = SleepColors.SlateBlue,
                indicatorColor = SleepColors.GlassSurface,
            ),
        )
    }
}

private fun NavController.navigateToBottomNavTab(route: String) {
    navigate(route) {
        popUpTo(Routes.CALCULATOR) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
