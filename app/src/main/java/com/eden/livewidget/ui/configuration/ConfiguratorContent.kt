package com.eden.livewidget.ui.configuration

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eden.livewidget.data.utils.Provider
import kotlinx.serialization.Serializable

@Serializable
object SelectProvider
@Serializable
object SelectPoint

@Composable
fun ConfiguratorContent(createWidget: (apiProvider: Provider, apiValue: String, displayName: String) -> Unit) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = SelectPoint) {
        composable<SelectProvider> { ConfiguratorSelectProviderScreen(navController) }
        composable<SelectPoint> { ConfiguratorSelectPointScreen(navController, createWidget) }
    }
}