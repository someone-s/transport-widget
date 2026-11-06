package com.eden.livewidget.ui.main.datasync

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.eden.livewidget.R
import com.eden.livewidget.data.agency.Agency
import com.eden.livewidget.ui.main.Providers
import com.eden.livewidget.ui.theme.TransportWidgetsTheme

@Composable
fun DataSyncScreen(
    agencies: List<Agency>,
    targetAgency: Agency? = null,
) {

    var inputKeyState by remember { mutableStateOf(false) }
    var currentKeyValue by remember { mutableStateOf("") }
    var currentInputKeyAction by remember { mutableStateOf<((String) -> Unit)?>(null) }

    var currentDownloadAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var downloadWarningState by remember { mutableStateOf(false) }

    var currentResetAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var resetWarningState by remember { mutableStateOf(false) }

    var placeWidgetState by remember { mutableStateOf(false) }

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

        InplaceSearch(agencies, lazyListState)
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
            items(agencies) { agency ->
                SourceContainer(
                    agency,
                    { currentInputKeyAction = it },
                    { currentKeyValue = it },
                    { inputKeyState = it },
                    { currentDownloadAction = it },
                    { downloadWarningState = it },
                    { currentResetAction = it },
                    { resetWarningState = it },
                )
                Spacer(Modifier.height(8.dp))
            }
            item { Spacer(Modifier.height(64.dp)) }
        }
    }

    PlaceWidgetButton({ placeWidgetState = it })
    if (placeWidgetState)
        PlaceWidgetGuide({ placeWidgetState = it })

    if (inputKeyState)
        KeyInputDialog(currentKeyValue, { inputKeyState = it }, currentInputKeyAction)

    if (downloadWarningState)
        DownloadWarningDialog({ downloadWarningState = it }, currentDownloadAction)

    if (resetWarningState)
        ResetWarningDialog({ resetWarningState = it }, currentResetAction)

    LaunchedEffect(true) {
        if (targetAgency != null)
            lazyListState.scrollToItem(agencies.indexOf(targetAgency))
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
                composable<Providers> { DataSyncScreen(emptyList(), null) }
            }
        }
    }
}