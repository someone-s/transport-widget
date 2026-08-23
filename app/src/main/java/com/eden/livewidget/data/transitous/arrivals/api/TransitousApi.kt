package com.eden.livewidget.data.transitous.arrivals.api

import android.content.Context
import android.util.Log
import com.eden.livewidget.data.common.arrivals.Model
import com.eden.livewidget.data.common.arrivals.api.Api
import com.eden.livewidget.data.common.points.Value
import com.eden.livewidget.data.transitous.points.TransitousValue as PointsTransitousValue
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

private data class TransitousResponse(
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

private data class TransitousPlaceSelf(
    @SerializedName("track")
    val track: String?,
    @SerializedName("departure")
    val departure: String?,
) {
    fun isValid() =
        track != null &&
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
    @GET("api/v6/stoptimes")
    fun getStopTimes(
        @Query("stopId")
        id: String,
        @Query("window")
        window: Int,
    ): Call<TransitousResponse>
}

class TransitousApi: Api {

    private val service: TransitousApiService by lazy {
        retrofit.create(TransitousApiService::class.java)
    }

    override suspend fun fetchLatestArrivals(
        context: Context,
        values: List<Value>
    ): List<Model> {

        assert(values.size == 1)
        val value = values[0] as PointsTransitousValue


        val request = service.getStopTimes(
            id = value.id,
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
        if (body !is TransitousResponse) {
            Log.i(this.javaClass.name, "no body")
            return emptyList()
        }

        if (!body.isValid()) {
            Log.i(this.javaClass.name, "body invalid")
            return emptyList()
        }

        val currentTime = LocalDateTime.now()

        val responseTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd'T'HH:mm:ss")
            .appendOffset("+HH:MM:SS", "Z")
            .toFormatter()

        return body.stopTimes!!
            .filter { it.isValid() }
            .map {

                checkNotNull(it.place)
                checkNotNull(it.tripTo)

                val expectDateTime =
                    ZonedDateTime
                        .from(responseTimeFormatter.parse(it.place.departure))
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime()

                val secondsToDeparture =
                    Math.toIntExact(ChronoUnit.SECONDS.between(currentTime, expectDateTime))

                Model(
                    operatorName = it.agencyName!!,
                    serviceName = it.routeName!!,
                    destinationName = it.tripTo.name!!,
                    platformName = it.place.track!!,
                    remainingS = secondsToDeparture,
                    expectedDateTime = expectDateTime,
                )
            }
    }
}