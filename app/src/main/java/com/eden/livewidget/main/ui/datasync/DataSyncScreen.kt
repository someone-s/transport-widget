package com.eden.livewidget.main.ui.datasync

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eden.livewidget.Agency
import com.eden.livewidget.R
import com.eden.livewidget.main.ui.Providers
import com.eden.livewidget.ui.theme.TransportWidgetsTheme

// context nullable for preview only
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSyncScreen(
    context: Context?,
    agency: Agency? = null
) {

    val (inputKeyState, setInputKeyState) = remember { mutableStateOf(false) }
    val (currentKeyValue, setCurrentKeyValueAction) = remember { mutableStateOf("") }
    val (currentInputKeyAction, setCurrentInputKeyAction) = remember { mutableStateOf<((String) -> Unit)?>(null) }

    val (currentDownloadAction, setCurrentDownloadAction) = remember { mutableStateOf<(() -> Unit)?>(null) }
    val (downloadWarningState, setDownloadWarningState) = remember { mutableStateOf(false) }

    val (currentResetAction, setCurrentResetAction) = remember { mutableStateOf<(() -> Unit)?>(null) }
    val (resetWarningState, setResetWarningState) = remember { mutableStateOf(false) }

    val (placeWidgetState, setPlaceWidgetState) = remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    Column {

        Spacer(Modifier.height(40.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                painter = painterResource(R.drawable.ic_shared_outlined_corporate_fare),
                contentDescription = stringResource(R.string.data_sync_screen_icon_description),
                modifier = Modifier
                    .size(52.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.navigation_providers),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.width(16.dp))
        }
        Spacer(Modifier.height(32.dp))

        InplaceSearch(lazyListState)
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .width(480.dp)
                .align(Alignment.CenterHorizontally),
            state = lazyListState,
            contentPadding = PaddingValues(
                top = 0.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 8.dp
            ),

            ) {
            itemsIndexed(Agency.entries) { _, agency ->
                SourceContainer(
                    agency,
                    setCurrentInputKeyAction, setCurrentKeyValueAction, setInputKeyState,
                    setCurrentDownloadAction, setDownloadWarningState,
                    setCurrentResetAction, setResetWarningState,
                )
                Spacer(Modifier.height(8.dp))
            }
            item { Spacer(Modifier.height(64.dp)) }
        }
    }

    PlaceWidgetButton(context, setPlaceWidgetState)
    if (placeWidgetState)
        PlaceWidgetGuide(setPlaceWidgetState)

    if (inputKeyState)
        KeyInputDialog(currentKeyValue, setInputKeyState, currentInputKeyAction)

    if (downloadWarningState)
        DownloadWarningDialog(setDownloadWarningState, currentDownloadAction)

    if (resetWarningState)
        ResetWarningDialog(setResetWarningState, currentResetAction)

    LaunchedEffect(true) {
        if (agency != null)
            lazyListState.scrollToItem(Agency.entries.indexOf(agency))
    }
}


@PreviewScreenSizes
@Composable
fun DataSyncScreenPreview() {
    val navController = rememberNavController()

    TransportWidgetsTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {}
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = Providers, Modifier.padding(innerPadding)) {
                composable<Providers> { DataSyncScreen(null) }
            }
        }
    }
}