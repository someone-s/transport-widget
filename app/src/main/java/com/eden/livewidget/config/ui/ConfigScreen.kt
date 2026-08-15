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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.eden.livewidget.config.PointInfo
import com.eden.livewidget.ui.theme.TransportWidgetsTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConfigScreen(
    currentAgency: Agency?,
    setCurrentAgency: (Agency?) -> Unit,
    currentPoint: PointInfo?,
    setCurrentPoint: (PointInfo?) -> Unit,
    anyChanges: Boolean,
    onSaveChanges: () -> Unit,
    onDiscardChanges: () -> Unit,
    forceAllVisible: Boolean = false,
) {

    val lazyListState = rememberLazyListState()

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
            LazyColumn(
                modifier = Modifier
                    .width(480.dp)
                    .fillMaxHeight(),
                state = lazyListState,
                contentPadding = PaddingValues(
                    top = 0.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                )
            ) {
                item {
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
                            AgencyItem(
                                currentAgency = currentAgency,
                                onAgencySelected = setCurrentAgency,
                            )
                        }
                    }
                }

                item {
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
                            PointItem(
                                currentProvider = currentAgency?.apiProvider,
                                currentPoint = currentPoint,
                                onPointSelected = setCurrentPoint,
                            )
                        }
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
            setCurrentPoint = {},
            anyChanges = false,
            onSaveChanges = {},
            onDiscardChanges = {},
            forceAllVisible = true,
        )
    }
}