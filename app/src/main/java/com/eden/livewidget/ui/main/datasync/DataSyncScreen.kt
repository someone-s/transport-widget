package com.eden.livewidget.ui.main.datasync

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import com.eden.livewidget.ui.main.datasync.place.PlaceWidgetButton
import com.eden.livewidget.ui.main.navigation.CommonRouteState
import com.eden.livewidget.ui.main.navigation.populateScope

@Composable
fun DataSyncScreen(
    sharedContentKey: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomBarState: CommonRouteState,
) = with(sharedTransitionScope)  {

    val hapticFeedback = LocalHapticFeedback.current

    NavigationSuiteScaffold(
        navigationSuiteItems = { bottomBarState.populateScope(this, onBeforeNavigate = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
        }) },
        modifier = Modifier.sharedElement(
            sharedContentState = rememberSharedContentState(sharedContentKey),
            animatedVisibilityScope = animatedVisibilityScope
        ),
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { DataSyncTopBar(scrollBehavior) },
            floatingActionButton = { PlaceWidgetButton() },
            content = { paddingValues -> DataSyncContent(modifier = Modifier.padding(paddingValues)) }
        )
    }
}