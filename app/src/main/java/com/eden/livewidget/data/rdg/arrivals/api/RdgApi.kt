package com.eden.livewidget.data.rdg.arrivals.api

import android.content.Context
import android.util.Log
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.common.arrivals.Model
import com.eden.livewidget.data.common.arrivals.api.Api
import com.eden.livewidget.data.common.keys.KeyPurpose
import com.eden.livewidget.data.common.keys.getKeyProviderConstructor
import com.eden.livewidget.data.common.points.Value
import com.eden.livewidget.data.rdg.arrivals.AugmentedRdgValue
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
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
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

private interface RdgApiService {
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
        timeWindowMinutes: Int,
        @Query("filterCRS")
        filterCrsCode: String? = null,
        @Query("filterType")
        filterMode: String? = null,
    ): Call<RdgDepartureResponse>
}

class RdgApi : Api {

    private val service: RdgApiService by lazy {
        retrofit.create(RdgApiService::class.java)
    }

    override suspend fun fetchLatestArrivals(
        context: Context,
        values: List<Value>,
    ): List<Model> {

        assert(values.size == 1)
        val value = values[0] as AugmentedRdgValue
        val crsCode = value.crsCode
        val toCrsCodes = value.toCrsCodes

        val currentTime = LocalDateTime.now()
        val requestTimeFormatter = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss")
        val currentTimeText = currentTime.format(requestTimeFormatter)

        Log.i(this.javaClass.name, "Data fetching")

        return if (toCrsCodes.isEmpty())
            fetchIteration(context, crsCode, currentTimeText, currentTime)
        else
            toCrsCodes
                .flatMap { toCrsCode ->
                    fetchIteration(context, crsCode, currentTimeText, currentTime, toCrsCode)
                }
                .sortedBy { model -> model.remainingS }
                .distinct()
    }

    private fun fetchIteration(
        context: Context,
        crsCode: String,
        currentTimeText: String,
        currentTime: LocalDateTime?,
        toCrsCode: String? = null,
    ): List<Model> {
        val headers = mapOf(
            "x-apikey" to Provider.RDG
                .keyProviders.getKeyProviderConstructor(KeyPurpose.ARRIVALS)!!()
                .getKey(context)
        )
        val request = service.getDepartureBoardWithDetails(
            headers = headers,
            crsCode = crsCode,
            timeText = currentTimeText,
            maxCount = 30,
            timeWindowMinutes = 60,
            filterCrsCode = toCrsCode,
            filterMode = if (toCrsCode != null) "to" else null,
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

        val unauthorizedCode = 401
        if (response.code() == unauthorizedCode) {
            Log.w(this.javaClass.name, "unauthorized")
            throw Api.AuthenticationException("Unable to authenticate")
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

        val responseTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 9, true)
            .toFormatter()

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
                    Model(
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
    }

    private val stripViaTextRegex = Regex("[Vv]ia ")
    private fun processViaText(input: String): String {
        return stripViaTextRegex.replace(input, "")
    }

    private val stripOperatorNameRegex = Regex("(?<=\\S)[^A-Z]")
    private fun processOperatorName(input: String): String {
        return if (input.length <= 8)
            input
        else
            stripOperatorNameRegex.replace(input, "").take(5)
    }
}