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
import com.example.a90phase.presentation.screens.discovery.DiscoveryResultsScreen
import com.example.a90phase.presentation.screens.history.HistoryScreen
import com.example.a90phase.presentation.screens.history.LogDetailScreen
import com.example.a90phase.presentation.screens.onboarding.OnboardingScreen
import com.example.a90phase.presentation.screens.settings.SettingsScreen
import com.example.a90phase.presentation.screens.splash.SplashScreen
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography

private val bottomNavRoutes = setOf(Route.Calculator.path, Route.History.path)

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
        startDestination = Route.Splash.path,
        modifier = modifier,
    ) {
        authRoutes(navController)
        mainRoutes(navController)
    }
}

private fun NavGraphBuilder.authRoutes(navController: NavHostController) {
    composable(Route.Splash.path) {
        ScreenFadeIn {
            SplashScreen(
                onOnboardingNeeded = {
                    navController.navigate(Route.Onboarding.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                },
                onGoToMain = {
                    navController.navigate(Route.Calculator.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                },
            )
        }
    }
    composable(Route.Onboarding.path) {
        ScreenFadeIn {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Route.Calculator.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                },
            )
        }
    }
}

private fun NavGraphBuilder.mainRoutes(navController: NavHostController) {
    composable(Route.Calculator.path) {
        ScreenFadeIn {
            CalculatorScreen(onNavigateToSettings = { navController.navigate(Route.Settings.path) })
        }
    }
    composable(Route.History.path) {
        ScreenFadeIn {
            HistoryScreen(
                onNavigateToLogDetail = { logId ->
                    navController.navigate(Route.LogDetail.build(logId))
                },
            )
        }
    }
    composable(Route.Settings.path) {
        ScreenFadeIn {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDiscoveryResults = {
                    navController.navigate(Route.DiscoveryResults.path)
                },
            )
        }
    }
    composable(Route.DiscoveryResults.path) {
        ScreenFadeIn {
            DiscoveryResultsScreen(
                onApply = { navController.popBackStack() },
                onDismiss = { navController.popBackStack() },
            )
        }
    }
    composable(
        route = Route.LogDetail.path,
        arguments = listOf(navArgument(Route.LogDetail.ARG_LOG_ID) { type = NavType.StringType }),
    ) { backStackEntry ->
        val logId = backStackEntry.arguments?.getString(Route.LogDetail.ARG_LOG_ID).orEmpty()
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
            selected = currentRoute == Route.Calculator.path,
            onClick = { onNavigate(Route.Calculator.path) },
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
            selected = currentRoute == Route.History.path,
            onClick = { onNavigate(Route.History.path) },
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
        popUpTo(Route.Calculator.path) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
