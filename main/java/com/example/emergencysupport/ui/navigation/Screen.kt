package com.example.emergencysupport.ui.navigation

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Resources : Screen("resources")
    data object Planner : Screen("planner")
    data object Alarm : Screen("alarm")
    data object Guide : Screen("guide")
    data object AiAssistant : Screen("ai_assistant")
    data object Checklist : Screen("checklist")
    data object Profile : Screen("profile")
}
