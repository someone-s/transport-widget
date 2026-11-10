package com.eden.livewidget.ui.main.navigation

import com.eden.livewidget.R
import kotlinx.serialization.Serializable


data class TopLevelRoute<T : Any>(
    val name: Int,
    val route: T,
    val selectedIcon: Int,
    val unselectedIcon: Int
)

val topLevelRoutes = listOf(
    TopLevelRoute(
        R.string.ui_main_navigation_toplevelroute_providers,
        DataSyncRoute,
        R.drawable.corporate_fare_filled,
        R.drawable.corporate_fare
    ),
    TopLevelRoute(
        R.string.ui_main_navigation_toplevelroute_about,
        AboutRoute,
        R.drawable.info_filled,
        R.drawable.info
    ),
)

@Serializable
object DataSyncRoute

@Serializable
object AboutRoute