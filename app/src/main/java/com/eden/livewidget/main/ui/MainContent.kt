package com.eden.livewidget.main.ui

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eden.livewidget.Agency
import com.eden.livewidget.R
import com.eden.livewidget.main.ui.about.AboutScreen
import com.eden.livewidget.main.ui.datasync.DataSyncScreen
import kotlinx.serialization.Serializable


data class TopLevelRoute<T : Any>(
    val name: String,
    val route: T,
    val selectedIcon: Painter,
    val unselectedIcon: Painter
)

@Serializable
object Providers

@Serializable
object About

@Composable
fun MainContent(
    applicationContext: Context,
    activityContext: Context,
    agency: Agency? = null
) {
    val topLevelRoutes = listOf(
        TopLevelRoute(
            stringResource(R.string.navigation_providers),
            Providers,
            painterResource(R.drawable.ic_shared_outlined_corporate_fare),
            painterResource(R.drawable.ic_shared_outlined_corporate_fare)
        ),
        TopLevelRoute(
            stringResource(R.string.navigation_about),
            About,
            painterResource(R.drawable.ic_shared_outlined_info),
            painterResource(R.drawable.ic_shared_outlined_info)
        ),
    )

    val navController = rememberNavController()

    // See https://developer.android.com/develop/ui/compose/navigation
    Scaffold(
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                topLevelRoutes.forEach { topLevelRoute ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = if (currentDestination?.hierarchy?.any {
                                        it.hasRoute(
                                            topLevelRoute.route::class
                                        )
                                    } == true)
                                    topLevelRoute.selectedIcon
                                else
                                    topLevelRoute.unselectedIcon,
                                contentDescription = topLevelRoute.name
                            )
                        },
                        label = { Text(topLevelRoute.name) },
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true,
                        onClick = {
                            navController.navigate(topLevelRoute.route) {

                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }

                                launchSingleTop = true

                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Providers, Modifier.padding(innerPadding)) {
            composable<Providers> { DataSyncScreen(applicationContext, agency) }
            composable<About> { AboutScreen(activityContext) }
        }

        if (
            agency != null &&
            navController.currentBackStackEntry?.destination?.hasRoute(Providers::class) == false
        )
            navController.navigate(route = Providers)
    }

}
