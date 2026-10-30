package com.eden.livewidget.ui.widget.active.fetching

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import com.eden.livewidget.R

@Composable
fun ActiveUnInitializedBlock() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
    ) {
        FetchingSurface(
            fetchMessage = LocalContext.current.getString(R.string.widget_active_loading_text)
        )
    }
}

