package com.eden.livewidget.data

import android.content.Context
import com.eden.livewidget.data.common.arrivals.DataSource as ArrivalsDataSource
import com.eden.livewidget.data.common.arrivals.Constructors as ArrivalsConstructors
import com.eden.livewidget.data.common.keys.KeyProviderConstructors
import com.eden.livewidget.data.common.keys.KeyPurpose
import com.eden.livewidget.data.common.keys.ObscuredKeyProvider
import com.eden.livewidget.data.common.points.Constructors as PointsConstructors
import com.eden.livewidget.data.common.points.Value as PointsValue
import com.eden.livewidget.data.common.points.datasource.RemoteDataSource as PointsRemoteDataSource
import com.eden.livewidget.data.common.points.cache.DatabaseProvider
import com.eden.livewidget.data.rdg.arrivals.filter.destination.RdgPreFetchExecutor
import com.eden.livewidget.data.common.arrivals.filter.destination.Value as FilterDestinationValue
import com.eden.livewidget.data.rdg.arrivals.api.RdgApi as ArrivalsRdgApi
import com.eden.livewidget.data.rdg.arrivals.filter.destination.RdgValue as FilterDestinationRdgValue
import com.eden.livewidget.data.rdg.arrivals.filter.destination.RdgCompiler as DestinationRdgCompiler
import com.eden.livewidget.data.rdg.points.RdgValue as PointsRdgValue
import com.eden.livewidget.data.rdg.points.cache.RdgDatabase
import com.eden.livewidget.data.rdg.points.cache.RdgDatabaseInfo
import com.eden.livewidget.data.rdg.points.api.RdgApi as PointsRdgApi
import com.eden.livewidget.data.tfl.arrivals.api.TflApi as ArrivalsTflApi
import com.eden.livewidget.data.tfl.arrivals.filter.destination.TflPostFetchExecutor
import com.eden.livewidget.data.tfl.arrivals.filter.destination.TflPreFetchExecutor
import com.eden.livewidget.data.tfl.arrivals.filter.destination.TflValue as FilterDestinationTflValue
import com.eden.livewidget.data.tfl.arrivals.filter.destination.TflCompiler as DestinationTflCompiler
import com.eden.livewidget.data.tfl.points.TflValue as PointsTflValue
import com.eden.livewidget.data.tfl.points.cache.TflDatabase
import com.eden.livewidget.data.tfl.points.cache.TflDatabaseInfo
import com.eden.livewidget.data.tfl.points.api.TflApi as PointsTflApi
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.util.EnumSet

val format = Json {
    serializersModule = SerializersModule {
        polymorphic(PointsValue::class) {
            subclass(PointsTflValue::class)
            subclass(PointsRdgValue::class)
        }
        polymorphic(FilterDestinationValue::class) {
            subclass(FilterDestinationTflValue::class)
            subclass(FilterDestinationRdgValue::class)
        }
    }
}

enum class Provider(
    val pointsConstructors: PointsConstructors,
    val arrivalsConstructors: ArrivalsConstructors,
    val keyProviders: KeyProviderConstructors = emptyMap()
) {
    TFL(
        pointsConstructors = PointsConstructors(
            dataSourceConstructor = {
                PointsRemoteDataSource(
                    pointsApi = PointsTflApi(),
                    cacheProvider = DatabaseProvider(
                        info = TflDatabaseInfo(),
                        klass = TflDatabase::class.java,
                    ),
                    ioDispatcher = Dispatchers.IO
                )
            }
        ),
        arrivalsConstructors = ArrivalsConstructors(
            dataSourceConstructor = {
                ArrivalsDataSource(
                    ArrivalsTflApi(),
                    Dispatchers.IO
                )
            },
            destinationCompilerConstructor = {
                DestinationTflCompiler()
            },
            preFetchExecutorConstructor = { state ->
                listOf(
                    TflPreFetchExecutor(
                        values = state.destinationFilters
                            .flatMap { it.values!! }
                            .map { it as FilterDestinationTflValue }
                    )
                )
            },
            postFetchExecutorConstructor = { state ->
                listOf(
                    TflPostFetchExecutor(
                        values = state.destinationFilters
                            .flatMap { it.values!! }
                            .map { it as FilterDestinationTflValue }
                    )
                )
            },
        ),
    ),
    RDG(
        pointsConstructors = PointsConstructors(
            dataSourceConstructor = {
                PointsRemoteDataSource(
                    pointsApi = PointsRdgApi(
                        apiProvider = RDG
                    ),
                    cacheProvider = DatabaseProvider(
                        info = RdgDatabaseInfo(),
                        klass = RdgDatabase::class.java,
                    ),
                    ioDispatcher = Dispatchers.IO
                )
            },
        ),
        arrivalsConstructors = ArrivalsConstructors(
            dataSourceConstructor = {
                ArrivalsDataSource(
                    ArrivalsRdgApi(),
                    Dispatchers.IO
                )
            },
            destinationCompilerConstructor = {
                DestinationRdgCompiler()
            },
            preFetchExecutorConstructor = { state ->
                listOf(
                    RdgPreFetchExecutor(
                        values = state.destinationFilters
                            .flatMap { it.values!! }
                            .map { it as FilterDestinationRdgValue }
                    )
                )
            },
            postFetchExecutorConstructor = { _ ->
                listOf()
            },
        ),
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