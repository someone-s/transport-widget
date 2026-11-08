package com.eden.livewidget.ui.main.welcome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun WelcomeLogic(
    viewModel: WelcomeViewModel =
        if (LocalInspectionMode.current) mockWelcomeViewModel
        else viewModel(factory = WelcomeViewModelProvider.Factory)
) {
    val isVersionCodeGreaterThanStored by viewModel.isVersionCodeGreaterThanStored.collectAsState(null)
    val scope = rememberCoroutineScope()

    when (isVersionCodeGreaterThanStored) {
        null -> WelcomeDialog(
            appName = viewModel.appName,
            onDismiss = { scope.launch { viewModel.storeBuildVersionCode() } },
        )
        true -> ReleaseDialog(
            appName = viewModel.appName,
            versionName = viewModel.versionName,
            onDismiss = { scope.launch { viewModel.storeBuildVersionCode() } },
        )
        false -> {}
    }

}
