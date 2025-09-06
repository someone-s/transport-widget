package com.eden.livewidget.main.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eden.livewidget.R
import com.eden.livewidget.data.points.PointsRepository
import com.eden.livewidget.data.utils.Provider
import com.eden.livewidget.main.DataSyncWorker
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

// context nullable for preview only
@Composable
fun DataSyncScreen(context: Context?) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            DataSyncSourceContainer(context, Provider.TFL)
        }
    }
}

@Composable
fun DataSyncSourceContainer(context: Context?, apiProvider: Provider) {

    val flow =
        if (context != null)
            DataSyncWorker.getWorkInfoFlow(context, apiProvider)
                .map { workInfos ->
                    if (workInfos.isEmpty()) return@map false

                    for (info in workInfos) {
                        Log.i("AAAAA", "${info.id} ${info.state.isFinished}")
                        if (!info.state.isFinished)
                            return@map true
                    }
                    return@map false
                }
        else
            flow { }
    val fetching by flow.collectAsState(false)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                text = "Transport for London",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "London's transport authority, provides live tracking data for buses, underground and riverboat services",
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
                            DataSyncWorker.schedule(context, apiProvider)
                        },
                    ) {
                        Text(text = "Update Data")
                    }
                }
                FilledTonalIconButton(
                    onClick = {
                        if (context == null) return@FilledTonalIconButton
                        DataSyncWorker.cancelCurrentRequest(context, apiProvider)
                        PointsRepository.getInstance(context, apiProvider).reset(context)
                    },
                    enabled = true
                ) {
                    Icon(
                        painterResource(R.drawable.ic_data_sync_reset),
                        "Download Data"
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