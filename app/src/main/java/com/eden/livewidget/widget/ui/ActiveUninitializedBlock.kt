package com.eden.livewidget.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize

@Composable
fun ActiveUnInitializedBlock() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
    ) {
        FetchingSurface()
    }
}

