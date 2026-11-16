package com.eden.livewidget.ui.widget.active.valid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import com.eden.livewidget.data.agency.common.arrivals.ArrivalEntry
import com.eden.livewidget.data.widget.structure.DisplayOptions
import com.eden.livewidget.data.widget.structure.ListStyle
import java.time.format.DateTimeFormatter

private data class CommonKey(
    val a: Any? = null,
    val b: Any? = null,
    val c: Any? = null,
    val d: Any? = null,
    val e: Any? = null,
    val f: Any? = null,
)

@Composable
fun ArrivalsList(
    arrivals: List<ArrivalEntry>,
    style: ListStyle,
) {

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val groupedItems = remember(arrivals) {
        when (style) {
            ListStyle.SHOW_LATER_GROUPED -> arrivals
                .take(30)
                .groupBy {
                    CommonKey(
                        it.operatorName,
                        it.serviceName,
                        it.locationPretext,
                        it.locationSupplement,
                        it.platformName,
                        it.destinationName,
                    )
                }
                .map { it.value }
            ListStyle.SHOW_LATER_FULL -> arrivals
                .take(30)
                .map { listOf(it) }
            ListStyle.HIDE_LATER -> arrivals
                .take(30)
                .groupBy {
                    CommonKey(
                        it.operatorName,
                        it.serviceName,
                        it.locationPretext,
                        it.locationSupplement,
                        it.platformName,
                        it.destinationName,
                    )
                }
                .map { listOf(it.value.first()) }
        }

    }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(
            items = groupedItems,
            itemId = { index, _ ->
                index.toLong()
            },
            itemContent = { index, group ->
                Column {
                    ArrivalsMainItem(group.first(), timeFormatter)

                    group.subList(1, group.size).take(2).forEach {
                        Spacer(modifier = GlanceModifier.height(4.dp))

                        ArrivalsRepeatItem(it, timeFormatter)
                    }

                    if (index < arrivals.lastIndex)
                        Spacer(modifier = GlanceModifier.height(4.dp))
                }
            }
        )
        item {
            Spacer(modifier = GlanceModifier.height(16.dp))
        }
    }


}

