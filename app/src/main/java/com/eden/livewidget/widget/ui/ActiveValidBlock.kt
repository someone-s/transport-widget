package com.eden.livewidget.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import com.eden.livewidget.data.common.arrivals.Model

@Composable
fun ActiveValidBlock(arrivals: List<Model>) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
    ) {
        if (arrivals.isEmpty()) {
            EmptySurface()
        }
        else {
            ArrivalsList(arrivals)
        }
    }
}

