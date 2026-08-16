package com.eden.livewidget.data

import android.content.Context
import com.eden.livewidget.data.arrivals.ArrivalsApi
import com.eden.livewidget.data.arrivals.api.ArrivalsRdgApi
import com.eden.livewidget.data.arrivals.api.ArrivalsTflApi
import com.eden.livewidget.data.keys.KeyProviderConstructors
import com.eden.livewidget.data.keys.KeyPurpose
import com.eden.livewidget.data.keys.ObscuredKeyProvider
import com.eden.livewidget.data.points.PointsDataSource
import com.eden.livewidget.data.points.PointsRemoteDataSource
import com.eden.livewidget.data.points.cache.DuplicatesDatabaseProvider
import com.eden.livewidget.data.points.cache.SimpleDatabaseProvider
import com.eden.livewidget.data.points.remoteapi.PointsRemoteRdgApi
import com.eden.livewidget.data.points.remoteapi.PointsRemoteTflApi
import kotlinx.coroutines.Dispatchers
import java.util.EnumSet

enum class Provider(
    val pointsDataSourceConstructor: (context: Context) -> PointsDataSource,
    val arrivalsApiConstructor: (apiValue: String) -> ArrivalsApi,
    val keyProviders: KeyProviderConstructors = emptyMap()
) {
    TFL(
        pointsDataSourceConstructor = { _ ->
            PointsRemoteDataSource(
                pointsApi = PointsRemoteTflApi(),
                pointsCacheProvider = DuplicatesDatabaseProvider(
                    apiProvider = TFL,
                ),
                ioDispatcher = Dispatchers.IO
            )
        },
        arrivalsApiConstructor = { commaSeparatedNaptanIds ->
            ArrivalsTflApi(commaSeparatedNaptanIds)
        },
    ),
    RDG(
        pointsDataSourceConstructor = { _ ->
            PointsRemoteDataSource(
                pointsApi = PointsRemoteRdgApi(
                    apiProvider = RDG
                ),
                pointsCacheProvider = SimpleDatabaseProvider(
                    apiProvider = RDG,
                ),
                ioDispatcher = Dispatchers.IO
            )
        },
        arrivalsApiConstructor = { crsCode ->
            ArrivalsRdgApi(
                crsCode,
                RDG
            )
        },
        keyProviders = mapOf(
            EnumSet.of(KeyPurpose.POINTS) to { ObscuredKeyProvider($"${RDG.name}-${KeyPurpose.POINTS.name}") },
            EnumSet.of(KeyPurpose.ARRIVALS) to { ObscuredKeyProvider($"${RDG.name}-${KeyPurpose.ARRIVALS.name}") }
        )
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