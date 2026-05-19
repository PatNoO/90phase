package com.example.a90phase.presentation.navigation

sealed class Route(val path: String) {
    data object Splash : Route("splash")
    data object Onboarding : Route("onboarding")
    data object Calculator : Route("calculator")
    data object History : Route("history")
    data object Settings : Route("settings")
    data object DiscoveryResults : Route("discovery_results")

    object LogDetail : Route("log_detail/{logId}") {
        const val ARG_LOG_ID = "logId"
        fun build(logId: String) = "log_detail/$logId"
    }
}
