package com.eden.livewidget.ui.config

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eden.livewidget.R
import com.eden.livewidget.data.agency.Agency
import com.eden.livewidget.ui.config.agency.Item
import com.eden.livewidget.ui.config.common.LabelItem
import com.eden.livewidget.ui.config.destination.DestinationsGroup
import com.eden.livewidget.ui.config.point.PointItem
import com.eden.livewidget.data.agency.common.point.FixedLogicalPoint
import com.eden.livewidget.data.agency.common.point.LogicalPoint
import com.eden.livewidget.data.widget.structure.WidgetConfig
import com.eden.livewidget.logic.DestinationState
import com.eden.livewidget.logic.FiltersState
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import kotlin.uuid.Uuid

@Composable
fun ConfigScreen(
    appWidgetId: Int,
    viewModel: ConfigViewModel =
        if (LocalInspectionMode.current) mockConfigViewModel
        else viewModel(factory = ConfigViewModelProvider.Factory),
    onExit: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    var agency by remember { mutableStateOf<Agency?>(null) }
    val provider = agency?.provider
    var point by remember { mutableStateOf<LogicalPoint?>(null) }
    val destinations = remember { mutableStateMapOf<Uuid, DestinationState>() }
    LaunchedEffect(appWidgetId, viewModel) {
        val widgetConfig = viewModel.getWidgetConfig(appWidgetId)
        agency = widgetConfig?.agencyId?.let { viewModel.agencyFromId(it) }
        point = widgetConfig?.point
        widgetConfig?.filters?.destinations
            ?.map { Pair(Uuid.random(), it) }
            ?.let { destinations.putAll(it) }
    }
    var anyChanges by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.config_ui_configscreen_title),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onExit,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = stringResource(R.string.config_ui_configscreen_action),
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .width(480.dp)
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    .padding(
                        top = 0.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    ),
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter =
                        slideInVertically() +
                                expandVertically() +
                                fadeIn(),
                    exit =
                        slideOutVertically() +
                                shrinkVertically() +
                                fadeOut(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        LabelItem(stringResource(R.string.config_ui_configscreen_label_agency))
                        Spacer(modifier = Modifier.height(8.dp))
                        Item(
                            agencies = viewModel.agencies,
                            currentAgency = agency,
                            onAgencySelected = { newAgency ->
                                agency = newAgency
                                point = null
                                destinations.clear()
                                anyChanges = true
                            },
                        )
                    }
                }

                val pointItemVisible = provider != null
                AnimatedVisibility(
                    visible = pointItemVisible,
                    enter =
                        slideInVertically() +
                                expandVertically() +
                                fadeIn(),
                    exit =
                        slideOutVertically() +
                                shrinkVertically() +
                                fadeOut(),
                ) {
                    if (pointItemVisible)
                        Column {
                            Spacer(modifier = Modifier.height(24.dp))
                            LabelItem(stringResource(R.string.config_ui_configscreen_label_point))
                            Spacer(modifier = Modifier.height(8.dp))
                            PointItem(
                                provider = provider,
                                point = point,
                                onPointSelected = { newPoint ->
                                    point = newPoint
                                    destinations.clear()
                                    anyChanges = true
                                },
                            )
                        }
                }

                val capturedPoint = point
                val destinationsGroupVisible = provider != null && capturedPoint != null && capturedPoint is FixedLogicalPoint
                AnimatedVisibility(
                    visible = destinationsGroupVisible,
                    enter =
                        slideInVertically() +
                                expandVertically() +
                                fadeIn(),
                    exit =
                        slideOutVertically() +
                                shrinkVertically() +
                                fadeOut(),
                ) {
                    if (destinationsGroupVisible)
                        Column {
                            Spacer(modifier = Modifier.height(24.dp))
                            LabelItem(stringResource(R.string.config_ui_configscreen_label_destination))
                            Spacer(modifier = Modifier.height(8.dp))
                            DestinationsGroup(
                                provider = provider,
                                point = capturedPoint,
                                destinations = destinations,
                                addDestination = { newConfig ->
                                    val id = Uuid.random()
                                    destinations[id] = DestinationState(
                                        config = newConfig
                                    )
                                    coroutineScope.launch {
                                        val compiledFilters = withContext(Dispatchers.IO) {
                                            provider.destinationCompiler.compileFilter(newConfig)
                                        }
                                        destinations.replace(id, DestinationState(
                                            config = newConfig,
                                            filters = compiledFilters,
                                        ))
                                    }
                                    anyChanges = true
                                },
                                updateDestination = { id, newConfig ->
                                    destinations[id] = DestinationState(
                                        config = newConfig
                                    )
                                    coroutineScope.launch {
                                        val compiledFilters = withContext(Dispatchers.IO) {
                                            provider.destinationCompiler.compileFilter(newConfig)
                                        }
                                        destinations.replace(id, DestinationState(
                                            config = newConfig,
                                            filters = compiledFilters,
                                        ))
                                    }
                                    anyChanges = true
                                },
                                removeDestination = { id ->
                                    destinations.remove(id)
                                    anyChanges = true
                                },
                            )
                        }
                }
            }

            if (anyChanges && agency != null && point != null)
                ApplyButton(
                    onClick = {
                        coroutineScope
                            .launch {
                                viewModel.setWidgetConfig(
                                    WidgetConfig(
                                        appWidgetId = appWidgetId,
                                        agencyId = agency?.id,
                                        point = point,
                                        filters = FiltersState(
                                            destinations = if (point is FixedLogicalPoint)
                                                destinations.map { it.value }
                                            else emptyList(),
                                        ),
                                        fetchDuration = Duration.ofSeconds(90),
                                        fetchInterval = Duration.ofSeconds(30),
                                    )
                                )
                            }
                            .invokeOnCompletion { onExit() }
                    }
                )
        }
    }
}

@PreviewScreenSizes
@Composable
fun PreviewConfigScreen() {

    TransportWidgetsTheme {

        ConfigScreen(
            appWidgetId = -1,
            onExit = {},
        )
    }
}