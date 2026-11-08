package com.eden.livewidget.ui.main.datasync

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eden.livewidget.ui.theme.TransportWidgetsTheme

@Composable
fun DataSyncContent(
    modifier: Modifier = Modifier,
    viewModel: DataSyncViewModel =
        if (LocalInspectionMode.current) mockDataSyncViewModel
        else viewModel(factory = DataSyncViewModelProvider.Factory),
) {

    val lazyListState = rememberLazyListState()

    val agencies = viewModel.agencies

    Column(modifier = modifier) {

        InplaceSearch(agencies, lazyListState)
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.CenterHorizontally),
            state = lazyListState,
            contentPadding = PaddingValues(
                top = 0.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 8.dp
            ),

            ) {
            items(agencies) { agency ->
                SourceContainer(agency)
                Spacer(Modifier.height(8.dp))
            }
            item { Spacer(Modifier.height(64.dp)) }
        }
    }

    val targetAgencyId = LocalTargetAgencyId.current
    LaunchedEffect(targetAgencyId) {
        val targetAgencyIndex = agencies.indexOfFirst { it.id == targetAgencyId }
        if (targetAgencyIndex >= 0)
            lazyListState.scrollToItem(targetAgencyIndex)
    }
}


@PreviewScreenSizes
@Composable
fun DataSyncContentPreview() {
    TransportWidgetsTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {}
            }
        ) { innerPadding ->
            DataSyncContent(Modifier.padding(innerPadding))
        }
    }
}