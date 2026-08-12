package com.eden.livewidget.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import com.eden.livewidget.R

@Composable
fun ActiveRetryBlock() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
    ) {
        FetchingSurface(
            fetchMessage = LocalContext.current.getString(R.string.widget_active_retrying_text)
        )
    }
}

