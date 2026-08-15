package com.eden.livewidget.data.arrivals.api

import android.content.Context
import android.util.Log
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.arrivals.ArrivalModel
import com.eden.livewidget.data.arrivals.ArrivalsApi
import com.eden.livewidget.data.keys.KeyPurpose
import com.eden.livewidget.data.keys.getKeyProviderConstructor
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max

private data class RdgDepartureResponse(
    @SerializedName("trainServices")
    val services: List<RdgService>?
)
private data class RdgService(
    @SerializedName("operator")
    val operator: String,
    @SerializedName("trainid")
    val trainId: String,
    @SerializedName("destination")
    val destinations: List<RdgDestination>?,
    @SerializedName("platform")
    val platform: String?,
    @SerializedName("atdSpecified")
    val hasActualTime: Boolean,
    @SerializedName("atd")
    val actualTimeText: String,
    @SerializedName("etdSpecified")
    val hasEstimatedTime: Boolean,
    @SerializedName("etd")
    val estimatedTimeText: String,
    @SerializedName("stdSpecified")
    val hasScheduledTime: Boolean,
    @SerializedName("std")
    val scheduledTimeText: String,
)

private data class RdgDestination(
    @SerializedName("locationName")
    val locationName: String,
    @SerializedName("via")
    val viaText: String?,
)

private const val BASE_URL = "https://api1.raildata.org.uk"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private interface ArrivalsRdgApiService {
    @GET("1010-live-departure-board---staff-version1_0/LDBSVWS/api/20220120/GetDepBoardWithDetails/{crsCode}/{time}")
    fun getDepartureBoardWithDetails(
        @HeaderMap
        headers: Map<String, String>,
        @Path("crsCode")
        crsCode: String,
        @Path("time")
        timeText: String,
        @Query("numRows")
        maxCount: Int,
        @Query("timeWindow")
        timeWindowMinutes: Int
    ): Call<RdgDepartureResponse>
}

class ArrivalsRdgApi(
    private val crsCode: String,
    private val apiProvider: Provider
) : ArrivalsApi {

    private val service: ArrivalsRdgApiService by lazy {
        retrofit.create(ArrivalsRdgApiService::class.java)
    }

    override suspend fun fetchLatestArrivals(context: Context): List<ArrivalModel> {

        val currentTime = LocalDateTime.now()
        val requestTimeFormatter = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss")
        val currentTimeText = currentTime.format(requestTimeFormatter)

        Log.i(this.javaClass.name, "Data fetching")

        val headers = mapOf(
            "x-apikey" to apiProvider
                .keyProviders.getKeyProviderConstructor(KeyPurpose.ARRIVALS)!!()
                .getKey(context)
        )
        val request = service.getDepartureBoardWithDetails(
            headers,
            crsCode,
            currentTimeText,
            30,
            60
        )

        val response = try { request.execute() } catch (e: IOException) {
            Log.w(this.javaClass.name, e.message ?: "mo error message")
            throw ArrivalsApi.UnresolvedException("Unable to reach server")
        }
        if (response == null) {
            Log.w(this.javaClass.name, "no response")
            throw ArrivalsApi.UnreachableException("Response empty")
        }

        val unauthorizedCode = 401
        if (response.code() == unauthorizedCode) {
            Log.w(this.javaClass.name, "unauthorized")
            throw ArrivalsApi.AuthenticationException("Unable to authenticate")
        }

        val body = response.body()
        if (body !is RdgDepartureResponse) {
            Log.i(this.javaClass.name, "no body")
            return emptyList()
        }

        val services = body.services
        if (services == null) {
            Log.i(this.javaClass.name, "no services list")
            return emptyList()
        }

        val responseTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss")

        return services
            .filter { !it.destinations.isNullOrEmpty() || it.hasActualTime || it.hasEstimatedTime || it.hasScheduledTime }
            .mapNotNull { service ->
                try {
                    val expectTimeText =
                        if (service.hasActualTime)
                            service.actualTimeText
                        else if (service.hasEstimatedTime)
                            service.estimatedTimeText
                        else // (service.hasScheduledTime)
                            service.scheduledTimeText
                    val expectDateTime =
                        LocalDateTime.from(responseTimeFormatter.parse(expectTimeText))

                    val secondsToDeparture =
                        Math.toIntExact(ChronoUnit.SECONDS.between(currentTime, expectDateTime))

                    val firstDestination = service.destinations!!.first()

                    Log.i("ARRIVAL-INFO", secondsToDeparture.toString())
                    ArrivalModel(
                        operatorName = processOperatorName(service.operator),
                        serviceName = service.trainId,
                        destinationName = firstDestination.locationName,
                        viaText = if (firstDestination.viaText != null) processViaText(
                            firstDestination.viaText
                        ) else "",
                        platformName = service.platform ?: "",
                        remainingS = max(0, secondsToDeparture - 60),
                        expectedDateTime = expectDateTime
                    )
                } catch (e: Exception) {
                    Log.e("ARRIVAL-INFO", e.message.toString())
                    null
                }
            }
            .sortedBy { model -> model.remainingS }
    }

    private val stripViaTextRegex = Regex("[Vv]ia ")
    private fun processViaText(input: String): String {
        return stripViaTextRegex.replace(input, "")
    }

    private val stripOperatorNameRegex = Regex("(?<=[^\\s])[^A-Z]")
    private fun processOperatorName(input: String): String {
        return if (input.length <= 8)
            input
        else
            stripOperatorNameRegex.replace(input, "").take(5)
    }
}