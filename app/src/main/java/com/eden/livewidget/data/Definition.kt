package com.eden.livewidget.data

import android.content.Context
import com.eden.livewidget.data.common.arrivals.api.Api
import com.eden.livewidget.data.common.filter.State as FilterState
import com.eden.livewidget.data.common.filter.Constructors as FilterConstructors
import com.eden.livewidget.data.rdg.arrivals.api.RdgApi as ArrivalsRdgApi
import com.eden.livewidget.data.tfl.arrivals.api.TflApi as ArrivalsTflApi
import com.eden.livewidget.data.common.keys.KeyProviderConstructors
import com.eden.livewidget.data.common.keys.KeyPurpose
import com.eden.livewidget.data.common.keys.ObscuredKeyProvider
import com.eden.livewidget.data.common.points.Value
import com.eden.livewidget.data.common.points.datasource.DataSource
import com.eden.livewidget.data.common.points.datasource.RemoteDataSource
import com.eden.livewidget.data.common.points.cache.DatabaseProvider
import com.eden.livewidget.data.rdg.points.RdgValue
import com.eden.livewidget.data.rdg.points.cache.RdgDatabase
import com.eden.livewidget.data.rdg.points.cache.RdgDatabaseInfo
import com.eden.livewidget.data.tfl.points.TflValue
import com.eden.livewidget.data.tfl.points.cache.TflDatabase
import com.eden.livewidget.data.tfl.points.cache.TflDatabaseInfo
import com.eden.livewidget.data.rdg.points.api.RdgApi as PointsRdgApi
import com.eden.livewidget.data.tfl.points.api.TflApi as PointsTflApi
import com.eden.livewidget.data.tfl.filter.destination.TflCompiler as DestinationTflCompiler
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.util.EnumSet

val module = SerializersModule {
    polymorphic(Value::class) {
        subclass(TflValue::class)
        subclass(RdgValue::class)
    }
}
val pointsFormat = Json {
    serializersModule = module
}

enum class Provider(
    val dataSourceConstructor: (context: Context) -> DataSource,
    val filterConstructors: FilterConstructors,
    val arrivalsApiConstructor: (apiValue: String, filterState: FilterState) -> Api,
    val keyProviders: KeyProviderConstructors = emptyMap()
) {
    TFL(
        dataSourceConstructor = { _ ->
            RemoteDataSource(
                pointsApi = PointsTflApi(),
                cacheProvider = DatabaseProvider(
                    info = TflDatabaseInfo(),
                    klass = TflDatabase::class.java,
                ),
                ioDispatcher = Dispatchers.IO
            )
        },
        filterConstructors = FilterConstructors(
            destinationCompilerConstructor = { DestinationTflCompiler() },
        ),
        arrivalsApiConstructor = { commaSeparatedNaptanIds, filterState ->
            ArrivalsTflApi(
                commaSeparatedNaptanIds = commaSeparatedNaptanIds,
                filterState = filterState,
            )
        },
    ),
    RDG(
        dataSourceConstructor = { _ ->
            RemoteDataSource(
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
        filterConstructors = FilterConstructors(
            destinationCompilerConstructor = { DestinationTflCompiler() },
        ),
        arrivalsApiConstructor = { crsCode, filterState ->
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