package com.eden.livewidget.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.eden.livewidget.Agency
import com.eden.livewidget.data.arrivals.ArrivalModel
import com.eden.livewidget.main.MainActivity
import java.time.LocalDateTime

enum class MyContentMode {
    ACTIVE_VALID,
    ACTIVE_UNINITIALIZED,
    PAUSED_OK_READY,
    ACTIVE_RETRY,
    PAUSED_ERROR_UNKNOWN,
    PAUSED_ERROR_BATTERY,
    PAUSED_ERROR_AUTHENTICATE,
    PAUSED_ERROR_UNREACHABLE,
    PAUSED_ERROR_UNRESOLVED,
}

@Composable
fun MyContent(
    mode: MyContentMode,
    widgetId: Int,
    displayName: String,
    agency: Agency?,
    lastValidData: List<ArrivalModel>,
) {

    Scaffold(
        backgroundColor = GlanceTheme.colors.widgetBackground,
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(
                onClick = actionStartActivity<MainActivity>()
            ),
        horizontalPadding = 16.dp,
        titleBar = {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    ),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = GlanceModifier
                        .defaultWeight(),
                    text = displayName,
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = 25.sp,
                    ),

                    maxLines = 1
                )

                if (mode != MyContentMode.PAUSED_OK_READY)
                    ControlGroup(mode, widgetId)
            }
        }
    ) {
        when (mode) {
            MyContentMode.ACTIVE_VALID -> ActiveValidBlock(lastValidData)
            MyContentMode.ACTIVE_UNINITIALIZED -> ActiveUnInitializedBlock()
            MyContentMode.ACTIVE_RETRY -> ActiveRetryBlock()
            MyContentMode.PAUSED_OK_READY -> ReadyBlock()
            MyContentMode.PAUSED_ERROR_BATTERY -> BatteryErrorBlock()
            MyContentMode.PAUSED_ERROR_UNRESOLVED -> UnresolvedErrorBlock()
            MyContentMode.PAUSED_ERROR_UNREACHABLE -> UnreachableErrorBlock()
            MyContentMode.PAUSED_ERROR_AUTHENTICATE -> AuthenticateErrorBlock(agency)
            MyContentMode.PAUSED_ERROR_UNKNOWN -> UnknownErrorBlock()
        }
    }

}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 380, heightDp = 300)
@Composable
fun MyContentPreview() {
    GlanceTheme {
        MyContent(
            mode = MyContentMode.ACTIVE_VALID,
            widgetId = -1,
            displayName = "Display name",
            agency = null,
            lastValidData = listOf(
                ArrivalModel(
                    operatorName = "Operator 1",
                    serviceName = "Service 1",
                    destinationName = "Really long destination name to display name to display",
                    viaText = "via Intermediate, More text",
                    platformName = "PF2",
                    remainingS = 30,
                    expectedDateTime = LocalDateTime.of(2026, 8, 2, 3, 39)
                ),
                ArrivalModel(
                    operatorName = "Operator 1",
                    serviceName = "SRV2A",
                    destinationName = "Really short",
                    viaText = "",
                    platformName = "A",
                    remainingS = 240,
                    expectedDateTime = LocalDateTime.of(2026, 8, 2, 23, 42)
                ),
                ArrivalModel(
                    operatorName = "Operator 1",
                    serviceName = "Service 1",
                    destinationName = "Really long destination name to display name to display",
                    viaText = "via Intermediate, More text",
                    platformName = "23",
                    remainingS = 4096,
                    expectedDateTime = LocalDateTime.of(2026, 8, 2, 20, 54)
                ),
            )
        )
    }
}