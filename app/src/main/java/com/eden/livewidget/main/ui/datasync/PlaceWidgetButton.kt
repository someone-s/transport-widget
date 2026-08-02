package com.eden.livewidget.main.ui.datasync

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.eden.livewidget.R
import com.eden.livewidget.widget.LivePointWidget
import com.eden.livewidget.widget.LivePointWidgetReceiver
import kotlinx.coroutines.launch

@Composable
fun PlaceWidgetButton(context: Context?, setPlaceGuideState: (Boolean) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        val coroutineScope = rememberCoroutineScope()

        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.data_sync_place_widget_text)) },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_data_sync_place_widget_icon),
                    stringResource(R.string.data_sync_place_widget_icon_description)
                )
            },
            onClick = {
                coroutineScope.launch {
                    if (context == null) return@launch
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                            receiver = LivePointWidgetReceiver::class.java,
                            preview = LivePointWidget(),
                        )
                    else
                        setPlaceGuideState(true)
                }
            },
            modifier = Modifier.padding(16.dp)
        )

    }
}