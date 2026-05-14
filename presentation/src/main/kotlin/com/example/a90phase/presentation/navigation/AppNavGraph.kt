package com.example.a90phase.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
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
        composable(Routes.SPLASH) {
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
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Routes.CALCULATOR) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.CALCULATOR) {
            CalculatorScreen(onNavigateToSettings = { navController.navigate(Routes.SETTINGS) })
        }
        composable(Routes.HISTORY) {
            HistoryScreen(onNavigateToLogDetail = { logId -> navController.navigate(Routes.logDetail(logId)) })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.LOG_DETAIL,
            arguments = listOf(navArgument("logId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val logId = backStackEntry.arguments?.getString("logId").orEmpty()
            LogDetailScreen(logId = logId, onNavigateBack = { navController.popBackStack() })
        }
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
            label = { Text(text = "Kalkylator", style = SleepTypography.LabelMedium) },
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
            label = { Text(text = "Historik", style = SleepTypography.LabelMedium) },
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
