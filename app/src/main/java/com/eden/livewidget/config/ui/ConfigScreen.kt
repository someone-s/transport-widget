package com.eden.livewidget.config.ui

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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.eden.livewidget.Agency
import com.eden.livewidget.R
import com.eden.livewidget.config.ui.agency.Item
import com.eden.livewidget.config.ui.common.LabelItem
import com.eden.livewidget.config.ui.destination.Group
import com.eden.livewidget.config.ui.point.Item
import com.eden.livewidget.data.common.filter.destination.Filter
import com.eden.livewidget.data.common.filter.Status
import com.eden.livewidget.data.common.points.Model
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConfigScreen(
    currentAgency: Agency?,
    setCurrentAgency: (Agency?) -> Unit,
    currentPoint: Model?,
    setCurrentPoint: (Model?) -> Unit,
    destinationFilters: Map<Uuid, Filter>?,
    addDestinationFilter: (Filter) -> Unit,
    updateDestinationFilter: (Uuid, Filter) -> Unit,
    removeDestinationFilter: (Uuid) -> Unit,
    anyChanges: Boolean,
    onSaveChanges: () -> Unit,
    onDiscardChanges: () -> Unit,
    forceAllVisible: Boolean = false,
) {

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
                        onClick = onDiscardChanges,
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
                            currentAgency = currentAgency,
                            onAgencySelected = setCurrentAgency,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = forceAllVisible || currentAgency != null,
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
                        LabelItem(stringResource(R.string.config_ui_configscreen_label_point))
                        Spacer(modifier = Modifier.height(8.dp))
                        Item(
                            currentProvider = currentAgency?.apiProvider,
                            currentPoint = currentPoint,
                            onPointSelected = setCurrentPoint,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = forceAllVisible || (currentAgency != null && currentPoint != null),
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
                        LabelItem(stringResource(R.string.config_ui_configscreen_label_destination))
                        Spacer(modifier = Modifier.height(8.dp))
                        Group(
                            currentProvider = currentAgency?.apiProvider,
                            currentPoint = currentPoint,
                            filters = destinationFilters,
                            addFilter = addDestinationFilter,
                            updateFilter = updateDestinationFilter,
                            removeFilter = removeDestinationFilter,
                        )
                    }
                }
            }

            if (forceAllVisible || (anyChanges && currentAgency != null && currentPoint != null))
                ApplyButton(
                    onClick = onSaveChanges
                )
        }
    }
}

@PreviewScreenSizes
@Composable
fun PreviewConfigScreen() {

    TransportWidgetsTheme {

        ConfigScreen(
            currentAgency = null,
            setCurrentAgency = {},
            currentPoint = null,
            setCurrentPoint = { _ -> },
            destinationFilters = mapOf(
                Uuid.random() to Filter(
                    Model("", ""),
                    Model("dest", "123"),
                    null,
                    Status.PENDING
                ),
                Uuid.random() to Filter(
                    Model("", ""),
                    Model("dest", "123"),
                    null,
                    Status.APPLIED
                ),
            ),
            addDestinationFilter = { _ -> },
            updateDestinationFilter = { _, _ -> },
            removeDestinationFilter = { _ -> },
            anyChanges = false,
            onSaveChanges = {},
            onDiscardChanges = {},
            forceAllVisible = true,
        )
    }
}