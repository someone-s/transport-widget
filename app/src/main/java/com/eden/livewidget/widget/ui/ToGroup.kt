package com.eden.livewidget.widget.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.eden.livewidget.R

@Composable
fun ToGroup(
    context: Context,
    toNames: List<String>
) {
    Column() {
        toNames.forEach { toName ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    provider = ImageProvider(R.drawable.filter_arrow_right),
                    contentDescription = context.getString(R.string.widget_ui_togroup_filter),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground),
                    modifier = GlanceModifier.size(16.dp),
                )
                Spacer(
                    modifier = GlanceModifier.width(2.dp),
                )
                Text(
                    text = toName,
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = 16.sp,
                    ),

                    maxLines = 1
                )
            }
        }
    }
}