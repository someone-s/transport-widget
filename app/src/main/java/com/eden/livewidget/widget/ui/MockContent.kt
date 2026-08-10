package com.eden.livewidget.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import com.eden.livewidget.R
import com.eden.livewidget.data.arrivals.ArrivalModel
import java.time.LocalDateTime

@Composable
fun MockContent() {

    MyContent(
        mode = MyContentMode.ACTIVE,
        widgetId = -1,
        displayName = LocalContext.current.getString(R.string.widget_mock_station_text),
        agency = null,
        lastValidData = listOf(
            ArrivalModel(
                operatorName = LocalContext.current.getString(R.string.widget_mock_arrival_0_operator_text),
                serviceName = LocalContext.current.getString(R.string.widget_mock_arrival_0_service_text),
                destinationName = LocalContext.current.getString(R.string.widget_mock_arrival_0_destination_text),
                viaText = "",
                platformName = LocalContext.current.getString(R.string.widget_mock_arrival_0_platform_text),
                remainingS = 30,
                expectedDateTime = LocalDateTime.of(2026, 8, 2, 3, 39)
            ),
            ArrivalModel(
                operatorName = LocalContext.current.getString(R.string.widget_mock_arrival_1_operator_text),
                serviceName = LocalContext.current.getString(R.string.widget_mock_arrival_1_service_text),
                destinationName = LocalContext.current.getString(R.string.widget_mock_arrival_1_destination_text),
                viaText = "",
                platformName = LocalContext.current.getString(R.string.widget_mock_arrival_1_platform_text),
                remainingS = 240,
                expectedDateTime = LocalDateTime.of(2026, 8, 2, 20, 42)
            ),
            ArrivalModel(
                operatorName = LocalContext.current.getString(R.string.widget_mock_arrival_2_operator_text),
                serviceName = LocalContext.current.getString(R.string.widget_mock_arrival_2_service_text),
                destinationName = LocalContext.current.getString(R.string.widget_mock_arrival_2_destination_text),
                viaText = "",
                platformName = LocalContext.current.getString(R.string.widget_mock_arrival_2_platform_text),
                remainingS = 4096,
                expectedDateTime = LocalDateTime.of(2026, 8, 2, 20, 54)
            ),
        ),
    )

}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 430, heightDp = 300)
@Composable
fun MockContentPreview() {
    GlanceTheme {
        MockContent()
    }
}