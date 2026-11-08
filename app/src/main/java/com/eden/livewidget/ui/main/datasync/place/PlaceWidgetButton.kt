package com.eden.livewidget.ui.main.datasync.place

import android.os.Build
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.eden.livewidget.R
import com.eden.livewidget.ui.widget.LivePointWidget
import com.eden.livewidget.ui.widget.LivePointWidgetReceiver
import kotlinx.coroutines.launch

@Composable
fun PlaceWidgetButton() {
    val context = LocalContext.current

    var isGuideVisible by remember { mutableStateOf(false) }

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                        receiver = LivePointWidgetReceiver::class.java,
                        preview = LivePointWidget(),
                    )
                else
                    isGuideVisible = true
            }
        },
    )

    if (isGuideVisible)
        PlaceWidgetGuide { isGuideVisible = false }
}