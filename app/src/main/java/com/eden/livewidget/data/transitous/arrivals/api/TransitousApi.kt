package com.eden.livewidget.data.transitous.arrivals.api

import android.content.Context
import android.util.Log
import com.eden.livewidget.data.common.arrivals.Model
import com.eden.livewidget.data.common.arrivals.api.Api
import com.eden.livewidget.data.common.points.Value
import com.eden.livewidget.data.transitous.arrivals.AugmentedTransitousValue
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

private data class TransitousUnfilteredResponse(
    @SerializedName("stopTimes")
    val stopTimes: List<TransitousStopTime>?,
) {
    fun isValid() =
        stopTimes != null
}

private data class TransitousStopTime(
    @SerializedName("place")
    val place: TransitousPlaceSelf?,
    @SerializedName("tripTo")
    val tripTo: TransitousPlaceTo?,
    @SerializedName("agencyName")
    val agencyName: String?,
    @SerializedName("routeShortName")
    val routeName: String?,
) {
    fun isValid() =
        place != null && place.isValid() &&
        tripTo != null && tripTo.isValid() &&
        agencyName != null &&
        routeName != null
}

private data class TransitousFilteredResponse(
    @SerializedName("itineraries")
    val itineraries: List<TransitousItinerary>?,
) {
    fun isValid() =
        itineraries != null
}

private data class TransitousItinerary(
    @SerializedName("legs")
    val legs: List<TransitousLeg>?,
) {
    fun isValid() =
        legs != null
}

private data class TransitousLeg(
    @SerializedName("mode")
    val mode: String?,
    @SerializedName("from")
    val from: TransitousPlaceSelf?,
    @SerializedName("tripTo")
    val tripTo: TransitousPlaceTo?,
    @SerializedName("agencyName")
    val agencyName: String?,
    @SerializedName("routeShortName")
    val routeName: String?,
) {
    fun isValid() =
        mode != null &&
        from != null && from.isValid() &&
        tripTo != null && tripTo.isValid() &&
        agencyName != null &&
        routeName != null
}

private data class TransitousPlaceSelf(
    @SerializedName("track")
    val trackNullable: String?,
    @SerializedName("departure")
    val departure: String?,
) {
    fun isValid() =
        departure != null
}

private data class TransitousPlaceTo(
    @SerializedName("name")
    val name: String?,
) {
    fun isValid() =
        name != null
}

private const val BASE_URL = "https://api.transitous.org"

private val okHttpClient = OkHttpClient.Builder()
    .readTimeout(30, TimeUnit.SECONDS)
    .connectTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

private val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private interface TransitousApiService {

    @Headers("User-Agent: TransportWidget/2.2.0+ (https://github.com/someone-s/transport-widget)")
    @GET("api/v6/stoptimes")
    fun getStopTimes(
        @Query("stopId")
        id: String,
        @Query("n")
        count: Int,
        @Query("window")
        window: Int,
    ): Call<TransitousUnfilteredResponse>

    @Headers("User-Agent: TransportWidget/2.2.0+ (https://github.com/someone-s/transport-widget)")
    @GET("api/v6/plan")
    fun getItineraries(
        @Query("fromPlace")
        fromId: String,
        @Query("toPlace")
        toId: String,
        @Query("maxTransfers")
        maxTransfers: Int,
        @Query("searchWindow")
        window: Int,
        @Query("transitModes")
        commaSeparatedModes: String,
    ): Call<TransitousFilteredResponse>
}

val validFilterModes = setOf(
    "FLEX",
    "TRAM",
    "SUBWAY",
    "FERRY",
    "BUS",
    "COACH",
    "RAIL",
    "HIGHSPEED_RAIL",
    "LONG_DISTANCE",
    "NIGHT_RAIL",
    "REGIONAL_FAST_RAIL",
    "REGIONAL_RAIL",
    "SUBURBAN",
    "FUNICULAR",
    "AERIAL_LIFT",
    "AREAL_LIFT",
    "METRO",
    "CABLE_CAR",
)

val commaSeparatedValidFilterModes = validFilterModes.joinToString(",")

class TransitousApi: Api {

    private val service: TransitousApiService by lazy {
        retrofit.create(TransitousApiService::class.java)
    }

    override suspend fun fetchLatestArrivals(
        context: Context,
        values: List<Value>
    ): List<Model> {

        assert(values.size == 1)
        val value = values[0] as AugmentedTransitousValue


        val currentTime = LocalDateTime.now()

        val responseTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd'T'HH:mm:ss")
            .appendOffset("+HH:MM:SS", "Z")
            .toFormatter()

            if (value.toIds.isEmpty()) {
                val stopPoints = fetchUnfiltered(value.id) ?: return emptyList()
                return stopPoints
                    .filter { it.isValid() }
                    .map { constructModel(it, responseTimeFormatter, currentTime) }
            }
            else
                return value.toIds
                    .flatMap { toId -> fetchFiltered(value.id, toId) ?: return emptyList() }
                    .filter { it.isValid() }
                    .distinct()
                    .map { constructModel(it, responseTimeFormatter, currentTime) }
                    .sortedBy { model -> model.remainingS }
    }

    private fun constructModel(
        stopTime: TransitousStopTime,
        responseTimeFormatter: DateTimeFormatter,
        currentTime: LocalDateTime
    ): Model {
        checkNotNull(stopTime.place)
        checkNotNull(stopTime.tripTo)

        val expectDateTime =
            ZonedDateTime
                .from(responseTimeFormatter.parse(stopTime.place.departure))
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()

        val secondsToDeparture =
            Math.toIntExact(ChronoUnit.SECONDS.between(currentTime, expectDateTime))

        return Model(
            operatorName = stopTime.agencyName!!,
            serviceName = stopTime.routeName!!,
            destinationName = stopTime.tripTo.name!!,
            platformName = stopTime.place.trackNullable ?: "",
            remainingS = secondsToDeparture,
            expectedDateTime = expectDateTime,
        )
    }

    private fun fetchUnfiltered(
        id: String,
    ): List<TransitousStopTime>? {

        val request = service.getStopTimes(
            id = id,
            count = 10,
            window = 3600,
        )

        val response = try {
            request.execute()
        } catch (e: IOException) {
            Log.w(this.javaClass.name, e.message ?: "mo error message")
            throw Api.UnresolvedException("Unable to reach server")
        }
        if (response == null) {
            Log.w(this.javaClass.name, "no response")
            throw Api.UnreachableException("Response empty")
        }

        val body = response.body()
        if (body !is TransitousUnfilteredResponse) {
            Log.i(this.javaClass.name, "no body")
            return null
        }

        if (!body.isValid()) {
            Log.i(this.javaClass.name, "body invalid")
            return null
        }

        return body.stopTimes
    }

    private fun fetchFiltered(
        fromId: String,
        toId: String
    ): List<TransitousStopTime>? {

        val request = service.getItineraries(
            fromId = fromId,
            toId = toId,
            maxTransfers = 0,
            window = 60,
            commaSeparatedModes = commaSeparatedValidFilterModes,
        )

        val response = try {
            request.execute()
        } catch (e: IOException) {
            Log.w(this.javaClass.name, e.message ?: "mo error message")
            throw Api.UnresolvedException("Unable to reach server")
        }
        if (response == null) {
            Log.w(this.javaClass.name, "no response")
            throw Api.UnreachableException("Response empty")
        }

        val body = response.body()
        if (body !is TransitousFilteredResponse) {
            Log.i(this.javaClass.name, "no body")
            return null
        }

        if (!body.isValid()) {
            Log.i(this.javaClass.name, "body invalid")
            return null
        }

        return body.itineraries!!
            .asSequence()
            .filter { it.isValid() }
            .flatMap { it.legs!! }
            .filter { it.isValid() }
            .map { leg ->
                TransitousStopTime(
                    place = leg.from,
                    tripTo = leg.tripTo,
                    agencyName = leg.agencyName,
                    routeName = leg.routeName,
                )
            }
            .toList()
    }
}