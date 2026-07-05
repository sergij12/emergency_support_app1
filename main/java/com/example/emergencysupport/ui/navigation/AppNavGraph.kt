package com.example.emergencysupport.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.emergencysupport.ui.component.BottomNavBar
import com.example.emergencysupport.ui.screen.ai.AiAssistantScreen
import com.example.emergencysupport.ui.screen.alarm.AlarmScreen
import com.example.emergencysupport.ui.screen.auth.AuthScreen
import com.example.emergencysupport.ui.screen.checklist.ChecklistScreen
import com.example.emergencysupport.ui.screen.guide.GuideScreen
import com.example.emergencysupport.ui.screen.home.HomeScreen
import com.example.emergencysupport.ui.screen.onboarding.OnboardingScreen
import com.example.emergencysupport.ui.screen.planner.PlannerScreen
import com.example.emergencysupport.ui.screen.profile.ProfileScreen
import com.example.emergencysupport.ui.screen.resources.ResourcesScreen
import com.example.emergencysupport.ui.viewmodel.MainViewModel

@Composable
fun AppNavGraph(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    val startDestination = when {
        !profile.isLoggedIn -> Screen.Auth.route
        isFirstLaunch -> Screen.Onboarding.route
        else -> Screen.Home.route
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute !in setOf(Screen.Onboarding.route, Screen.Auth.route)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(currentRoute = currentRoute) { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Auth.route) {
                AuthScreen(
                    onLogin = {
                        viewModel.login(it)
                        navController.navigate(
                            if (isFirstLaunch) Screen.Onboarding.route else Screen.Home.route
                        ) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onContinue = {
                        viewModel.finishOnboarding()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onOpenAlarm = { navController.navigate(Screen.Alarm.route) },
                    onOpenPlanner = { navController.navigate(Screen.Planner.route) },
                    onOpenResources = { navController.navigate(Screen.Resources.route) },
                    onOpenChecklist = { navController.navigate(Screen.Checklist.route) },
                    onOpenGuide = { navController.navigate(Screen.Guide.route) }
                )
            }

            composable(Screen.Resources.route) {
                ResourcesScreen(viewModel)
            }

            composable(Screen.Planner.route) {
                PlannerScreen(viewModel)
            }

            composable(Screen.Alarm.route) {
                AlarmScreen(viewModel)
            }

            composable(Screen.Guide.route) {
                GuideScreen()
            }

            composable(Screen.AiAssistant.route) {
                AiAssistantScreen(viewModel)
            }

            composable(Screen.Checklist.route) {
                ChecklistScreen(viewModel)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(viewModel)
            }
        }
    }
}