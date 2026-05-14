package com.example.a90phase.presentation.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val CALCULATOR = "calculator"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val LOG_DETAIL = "log_detail/{logId}"

    fun logDetail(logId: String) = "log_detail/$logId"
}
