package com.eden.livewidget.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import com.eden.livewidget.data.arrivals.ArrivalModel

@Composable
fun ArrivalsList(arrivals: List<ArrivalModel>) {

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(arrivals) { index, arrival ->
            Column {
                ArrivalsItem(arrival)

                if (index < arrivals.size - 1)
                    Spacer(modifier = GlanceModifier.height(4.dp))
            }

        }
        item {
            Spacer(modifier = GlanceModifier.height(16.dp))
        }
    }


}

