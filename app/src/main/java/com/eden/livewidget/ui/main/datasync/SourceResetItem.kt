package com.eden.livewidget.ui.main.datasync

import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eden.livewidget.R
import com.eden.livewidget.data.agency.Agency
import com.eden.livewidget.logic.sync.DataSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SourceResetItem(
    agency: Agency,
    setCurrentResetAction: ((() -> Unit)?) -> Unit,
    setResetWarningState: (Boolean) -> Unit,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    SegmentedListItem(
        shapes = shapes,
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_data_sync_reset),
                contentDescription = stringResource(R.string.data_sync_screen_reset_data_button_icon)
            )
        },
        content = {
            Text(
                text = stringResource(R.string.data_sync_screen_reset_data_button_main_text)
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.data_sync_screen_reset_data_button_support_text)
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        onClick = {
            setCurrentResetAction {
                DataSyncWorker.cancelCurrentRequest(context, agency)

                coroutineScope.launch(Dispatchers.IO) {
                    val resettablePointsRepository = agency.provider.pointsRepository.asResettable
                    checkNotNull(resettablePointsRepository)
                    resettablePointsRepository.reset(context)
                }
            }
            setResetWarningState(true)
        }
    )
}