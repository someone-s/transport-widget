package com.eden.livewidget.ui.main.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy

@Composable
fun rememberRouteState(
    initialTopLevelRoutes: List<TopLevelRoute<*>>,
): CommonRouteState {
    return remember {
        CommonRouteState(
            initialTopLevelRoutes
        )
    }
}

@Stable
class CommonRouteState(
    initialTopLevelRoutes: List<TopLevelRoute<*>>,
) {
    var currentDestination by mutableStateOf<NavDestination?>(null)
    val topLevelRoutes = initialTopLevelRoutes.toMutableStateList()
    var targetRoute by mutableStateOf<TopLevelRoute<*>?>(null)
}

fun CommonRouteState.populateScope(scope: NavigationSuiteScope, onBeforeNavigate: (() -> Unit)? = null): Unit = with(scope) {

    topLevelRoutes.forEach { topLevelRoute ->
        val selected = currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true
        item(
            selected = selected,
            onClick = {
                onBeforeNavigate?.invoke()
                targetRoute = topLevelRoute
            },
            icon = {
                Icon(
                    painter = painterResource(if (selected) topLevelRoute.selectedIcon else topLevelRoute.unselectedIcon),
                    contentDescription = null,
                )
            },
            label = {
                Text(stringResource(topLevelRoute.name))
            }
        )
    }
}