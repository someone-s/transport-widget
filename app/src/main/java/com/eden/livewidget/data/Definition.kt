package com.eden.livewidget.data

import android.content.Context
import com.eden.livewidget.data.common.arrivals.api.Api
import com.eden.livewidget.data.rdg.arrivals.api.RdgApi as ArrivalsRdgApi
import com.eden.livewidget.data.tfl.arrivals.api.TflApi as ArrivalsTflApi
import com.eden.livewidget.data.common.keys.KeyProviderConstructors
import com.eden.livewidget.data.common.keys.KeyPurpose
import com.eden.livewidget.data.common.keys.ObscuredKeyProvider
import com.eden.livewidget.data.common.points.datasource.DataSource
import com.eden.livewidget.data.common.points.datasource.RemoteDataSource
import com.eden.livewidget.data.common.points.cache.DuplicatesDatabaseProvider
import com.eden.livewidget.data.common.points.cache.SimpleDatabaseProvider
import com.eden.livewidget.data.rdg.points.api.RdgApi as PointsRdgApi
import com.eden.livewidget.data.tfl.points.api.TflApi as PointsTflApi
import kotlinx.coroutines.Dispatchers
import java.util.EnumSet

enum class Provider(
    val dataSourceConstructor: (context: Context) -> DataSource,
    val apiConstructor: (apiValue: String) -> Api,
    val keyProviders: KeyProviderConstructors = emptyMap()
) {
    TFL(
        dataSourceConstructor = { _ ->
            RemoteDataSource(
                pointsApi = PointsTflApi(),
                cacheProvider = DuplicatesDatabaseProvider(
                    apiProvider = TFL,
                ),
                ioDispatcher = Dispatchers.IO
            )
        },
        apiConstructor = { commaSeparatedNaptanIds ->
            ArrivalsTflApi(commaSeparatedNaptanIds)
        },
    ),
    RDG(
        dataSourceConstructor = { _ ->
            RemoteDataSource(
                pointsApi = PointsRdgApi(
                    apiProvider = RDG
                ),
                cacheProvider = SimpleDatabaseProvider(
                    apiProvider = RDG,
                ),
                ioDispatcher = Dispatchers.IO
            )
        },
        apiConstructor = { crsCode ->
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