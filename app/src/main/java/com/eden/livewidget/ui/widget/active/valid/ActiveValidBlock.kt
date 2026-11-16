package com.eden.livewidget.ui.widget.active.valid

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import com.eden.livewidget.data.agency.common.arrivals.ArrivalEntry
import com.eden.livewidget.data.widget.structure.DisplayOptions

@Composable
fun ActiveValidBlock(arrivals: List<ArrivalEntry>, displayOptions: DisplayOptions) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
    ) {
        if (arrivals.isEmpty()) {
            EmptySurface()
        }
        else {
            ArrivalsList(arrivals, displayOptions.listStyle)
        }
    }
}

