package com.eden.livewidget.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eden.livewidget.data.Provider
import kotlinx.serialization.Serializable

@Serializable
object SelectAgency
@Serializable
object SelectPoint

@Composable
fun ConfiguratorContent(createWidget: (apiProvider: Provider, apiValue: String, displayName: String) -> Unit) {
    val navController = rememberNavController()
    val apiProvider = remember { mutableStateOf(Provider.TFL) }

    NavHost(navController, startDestination = SelectAgency) {
        composable<SelectAgency> { ConfiguratorSelectProviderScreen(navController, apiProvider) }
        composable<SelectPoint> { ConfiguratorSelectPointScreen(navController, apiProvider, createWidget) }
    }
}