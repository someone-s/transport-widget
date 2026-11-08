package com.eden.livewidget.ui.main

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eden.livewidget.ui.main.about.AboutScreen
import com.eden.livewidget.ui.main.datasync.DataSyncScreen
import com.eden.livewidget.ui.main.datasync.LocalTargetAgencyId
import com.eden.livewidget.ui.main.navigation.AboutRoute
import com.eden.livewidget.ui.main.navigation.DataSyncRoute
import com.eden.livewidget.ui.main.navigation.rememberRouteState
import com.eden.livewidget.ui.main.navigation.topLevelRoutes

@Composable
fun MainContent() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val bottomBarState = rememberRouteState(topLevelRoutes)

    LaunchedEffect(navBackStackEntry) {
        bottomBarState.currentDestination = navBackStackEntry?.destination
    }

    LaunchedEffect(bottomBarState.targetRoute) {
        if (bottomBarState.targetRoute == null) return@LaunchedEffect

        navController.navigate(bottomBarState.targetRoute!!.route) {

            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }

            launchSingleTop = true

            restoreState = true
        }

        bottomBarState.targetRoute = null
    }

    val sharedContentKey = "SCAFFOLD"

    SharedTransitionLayout {
        NavHost(navController, startDestination = DataSyncRoute) {
            composable<DataSyncRoute> { DataSyncScreen(
                bottomBarState = bottomBarState,
                sharedContentKey = sharedContentKey,
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this,
            ) }
            composable<AboutRoute> { AboutScreen(
                bottomBarState = bottomBarState,
                sharedContentKey = sharedContentKey,
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this
            ) }
        }
    }

    val targetAgencyId = LocalTargetAgencyId.current
    LaunchedEffect(targetAgencyId) {
        if (targetAgencyId != null &&
            navController.currentBackStackEntry?.destination?.hasRoute(DataSyncRoute::class) == false)
            navController.navigate(route = DataSyncRoute)
    }

}
