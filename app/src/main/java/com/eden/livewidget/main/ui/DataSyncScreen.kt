package com.eden.livewidget.main.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eden.livewidget.Agency
import com.eden.livewidget.R
import com.eden.livewidget.data.points.PointsRepository
import com.eden.livewidget.main.DataSyncWorker
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import me.xdrop.fuzzywuzzy.FuzzySearch

// context nullable for preview only
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSyncScreen(context: Context?) {

    val (currentDownloadAction, setCurrentDownloadAction) = remember {
        mutableStateOf<(() -> Unit)?>(
            null
        )
    }
    val (downloadWarningState, setDownloadWarningState) = remember { mutableStateOf(false) }

    val (currentResetAction, setCurrentResetAction) = remember { mutableStateOf<(() -> Unit)?>(null) }
    val (resetWarningState, setResetWarningState) = remember { mutableStateOf(false) }


    val lazyListState = rememberLazyListState()

    Column {

        Spacer(Modifier.height(32.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Providers",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(
                    top = 32.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                )
            )
        }
        Spacer(Modifier.height(4.dp))

        InplaceSearch(lazyListState)
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(
                top = 0.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 8.dp
            ),

            ) {
            itemsIndexed(Agency.entries) { index, agency ->
                DataSyncSourceContainer(
                    context, agency,
                    setCurrentDownloadAction, setDownloadWarningState,
                    setCurrentResetAction, setResetWarningState,
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (downloadWarningState)
        DownloadWarningDialog(setDownloadWarningState, currentDownloadAction)
    if (resetWarningState)
        ResetWarningDialog(setResetWarningState, currentResetAction)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun InplaceSearch(lazyListState: LazyListState) {
    // Controls expansion state of the search bar
    val textFieldState = rememberTextFieldState()

    val agencyLookup: Map<String, Agency> =
        Agency.entries.associateBy { agency -> stringResource(agency.agencyName) }


    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = SearchBarDefaults.inputFieldShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            SearchBarDefaults.InputField(
                query = textFieldState.text.toString(),
                onQueryChange = {
                    textFieldState.edit { replace(0, length, it) }
                    val result = FuzzySearch.extractOne(it, agencyLookup.keys)
                    coroutineScope.launch {
                        lazyListState.scrollToItem(result.index)
                    }
                },
                onSearch = { },
                expanded = false,
                onExpandedChange = { },
                placeholder = { Text("Search for a provider") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = null,
            )
        }
    }
}

@Composable
private fun DownloadWarningDialog(
    setDownloadWarningState: (Boolean) -> Unit,
    currentDownloadAction: (() -> Unit)?,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = {
                setDownloadWarningState(false)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentDownloadAction != null)
                            currentDownloadAction()
                        setDownloadWarningState(false)
                    }
                ) { Text(stringResource(R.string.data_sync_download_warning_dialog_confirm)) }
            },
            dismissButton = {
                Button(onClick = {
                    setDownloadWarningState(false)
                }) { Text(stringResource(R.string.data_sync_download_warning_dialog_cancel)) }
            },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_data_sync_download),
                    stringResource(R.string.data_sync_download_warning_dialog_icon_description)
                )
            },
            title = { Text(stringResource(R.string.data_sync_download_warning_dialog_title)) },
            text = { Text(stringResource(R.string.data_sync_download_warning_dialog_body)) }
        )
    }
}


@Composable
private fun ResetWarningDialog(
    setResetWarningState: (Boolean) -> Unit,
    currentResetAction: (() -> Unit)?,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = {
                setResetWarningState(false)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentResetAction != null)
                            currentResetAction()
                        setResetWarningState(false)
                    }
                ) { Text(stringResource(R.string.data_sync_reset_warning_dialog_confirm)) }
            },
            dismissButton = {
                Button(onClick = {
                    setResetWarningState(false)
                }) { Text(stringResource(R.string.data_sync_reset_warning_dialog_cancel)) }
            },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_data_sync_reset),
                    stringResource(R.string.data_sync_reset_warning_dialog_icon_description)
                )
            },
            title = { Text(stringResource(R.string.data_sync_reset_warning_title)) },
            text = { Text(stringResource(R.string.data_sync_reset_warning_body)) }
        )
    }
}

@Composable
fun DataSyncSourceContainer(
    context: Context?, agency: Agency,
    setCurrentDownloadAction: ((() -> Unit)?) -> Unit, setDownloadWarningState: (Boolean) -> Unit,
    setCurrentResetAction: ((() -> Unit)?) -> Unit, setResetWarningState: (Boolean) -> Unit,
) {

    val flow = if (context != null) DataSyncWorker.getIsActiveFlow(
        context,
        agency.apiProvider
    ) else flow { }
    val fetching by flow.collectAsState(false)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween

        ) {
            Text(
                text = stringResource(agency.agencyName),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(agency.agencyDescription),
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (fetching) {
                    Column(
                        // component with weight is measured only after non-weight components
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Fetching all stops",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                } else {
                    FilledTonalButton(
                        onClick = {
                            if (context == null) return@FilledTonalButton
                            setCurrentDownloadAction {
                                DataSyncWorker.schedule(context, agency.apiProvider)
                            }
                            setDownloadWarningState(true)

                        },
                    ) {
                        Text(text = stringResource(R.string.data_sync_screen_update_data_button_text))
                    }
                }
                FilledTonalIconButton(
                    onClick = {
                        if (context == null) return@FilledTonalIconButton
                        setCurrentResetAction {
                            DataSyncWorker.cancelCurrentRequest(context, agency.apiProvider)
                            PointsRepository.getInstance(context, agency.apiProvider).reset(context)
                        }
                        setResetWarningState(true)
                    },
                    enabled = true
                ) {
                    Icon(
                        painterResource(R.drawable.ic_data_sync_reset),
                        stringResource(R.string.data_sync_screen_reset_data_button_description)
                    )
                }
            }
        }
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
            NavHost(navController, startDestination = Browse, Modifier.padding(innerPadding)) {
                composable<Browse> { DataSyncScreen(null) }
            }
        }
    }
}