package com.eden.livewidget.data

import android.content.Context
import com.eden.livewidget.data.arrivals.ArrivalsApi
import com.eden.livewidget.data.arrivals.api.ArrivalsTflApi
import com.eden.livewidget.data.keys.KeyProviderConstructors
import com.eden.livewidget.data.keys.KeyPurpose
import com.eden.livewidget.data.points.PointsDataSource
import com.eden.livewidget.data.points.PointsRemoteDataSource
import com.eden.livewidget.data.points.remoteapi.PointsRemoteTflApi
import kotlinx.coroutines.Dispatchers
import java.util.EnumSet

enum class Provider(
    val pointsDataSourceConstructor: (context: Context) -> PointsDataSource,
    val arrivalsApiConstructor: (apiValue: String) -> ArrivalsApi,
    val keyProviders: KeyProviderConstructors = emptyMap()
) {
    TFL(
        pointsDataSourceConstructor = { context ->
            PointsRemoteDataSource(
                context,
                PointsRemoteTflApi(
                    Dispatchers.IO
                ),
                TFL,
                Dispatchers.IO
            )
        },
        arrivalsApiConstructor = { stopPointId ->
            ArrivalsTflApi(stopPointId)
        }
    )
}

fun providerToString(provider: Provider): String = provider.name

fun providerFromString(string: String?): Provider? {

    if (string == null)
        return null

    var apiProvider: Provider
    try {
        apiProvider = Provider.valueOf(string)
    } catch (_: IllegalArgumentException) {
        return null
    }

    return apiProvider
}