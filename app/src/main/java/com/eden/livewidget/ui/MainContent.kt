package com.eden.livewidget.ui

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eden.livewidget.DataSyncWorker
import com.eden.livewidget.data.utils.Provider
import kotlinx.serialization.Serializable


data class TopLevelRoute<T : Any>(
    val name: String,
    val route: T,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Serializable
object Browse

@Serializable
object About

@Composable
fun MainContent(context: Context) {
    val topLevelRoutes = listOf(
        TopLevelRoute("Browse", Browse, Icons.Filled.Add, Icons.Outlined.Add),
        TopLevelRoute("About", About, Icons.Filled.Settings, Icons.Outlined.Settings),
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
                                imageVector =
                                    if (currentDestination?.hierarchy?.any {
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
        NavHost(navController, startDestination = Browse, Modifier.padding(innerPadding)) {
            composable<Browse> { BrowseContent(context) }
            composable<About> { AboutContent() }
        }
    }

}


@Composable
fun BrowseContent(context: Context) {

    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            DataSyncWorker.schedule(context, Provider.TFL)
        }
    ) {
        Text(
            text = "update TFL"
        )
    }
}

@Composable
fun AboutContent() {

}