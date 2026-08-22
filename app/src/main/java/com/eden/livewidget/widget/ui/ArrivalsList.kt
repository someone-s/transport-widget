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
import com.eden.livewidget.data.common.arrivals.Model
import java.time.format.DateTimeFormatter

@Composable
fun ArrivalsList(arrivals: List<Model>) {

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(arrivals.take(40)) { index, arrival ->
            Column {
                ArrivalsItem(arrival, timeFormatter)

                if (index < arrivals.size - 1)
                    Spacer(modifier = GlanceModifier.height(4.dp))
            }

        }
        item {
            Spacer(modifier = GlanceModifier.height(16.dp))
        }
    }


}

