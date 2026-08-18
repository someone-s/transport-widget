package com.eden.livewidget.data.tfl.arrivals.api

import android.content.Context
import android.util.Log
import com.eden.livewidget.data.common.arrivals.Model
import com.eden.livewidget.data.common.arrivals.api.Api
import com.eden.livewidget.data.common.points.Value
import com.eden.livewidget.data.tfl.points.TflValue
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.max

private data class TflEntry(
    @SerializedName("lineId")
    val lineId: String?,
    @SerializedName("lineName")
    val lineName: String?,
    @SerializedName("platformName")
    val platformName: String?,
    @SerializedName("destinationName")
    val destinationName: String?,
    @SerializedName("direction")
    val direction: String?,
    @SerializedName("towards")
    val towards: String?,
    @SerializedName("timeToStation")
    val timeToStation: Int?,
    @SerializedName("expectedArrival")
    val expectedArrivalString: String?,
) {
    fun isValid() =
        lineId != null &&
        lineName != null &&
        platformName != null &&
        destinationName != null &&
        direction != null &&
        towards != null &&
        timeToStation != null &&
        expectedArrivalString != null
}
private const val BASE_URL = "https://api.tfl.gov.uk"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private interface TflApiService {
    @GET("StopPoint/{id}/Arrivals")
    fun getStopPointArrivals(
        @Path("id")
        stopPointId: String,
    ): Call<List<TflEntry>>
}


class TflApi: Api {

//    private val filterLines: Map<String, List<String>> =
//        filterState.destinationFilters
//            .mapNotNull { filter -> filter.filterValue }
//            .flatMap { filterString -> LineWithDirection.deserializeList(filterString) }
//            .groupBy(
//                keySelector = { lineWithDirection -> lineWithDirection.lineId },
//                valueTransform = { lineWithDirection -> lineWithDirection.direction }
//            )


    private val service: TflApiService by lazy {
        retrofit.create(TflApiService::class.java)
    }

    override suspend fun fetchLatestArrivals(
        context: Context,
        values: List<Value>,
    ): List<Model> {

        val naptanIds = values.map { (it as TflValue).naptanId }

        val entries = mutableListOf<TflEntry>()

        coroutineScope {
            naptanIds.forEach { naptanId ->
                launch {
                    Log.i(this.javaClass.name, "Fetching data for $naptanId")
                    val request = service.getStopPointArrivals(naptanId)

                    val response = try { request.execute() } catch (e: IOException) {
                        Log.w(this.javaClass.name, e.message ?: "mo error message")
                        throw Api.UnresolvedException("Unable to reach server")
                    }
                    if (response == null) {
                        Log.w(this.javaClass.name, "no response")
                        throw Api.UnreachableException("Response empty")
                    }

                    val body = response.body()
                    if (body !is List<TflEntry>) {
                        Log.i(this.javaClass.name, "no body")
                        return@launch
                    }

                    entries.addAll(body)
                }
            }
        }

        entries.sortBy { entry -> entry.timeToStation }


        val responseTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'")

        return entries
            .filter {
                it.isValid()
            }
//            .filter {
//                if (filterLines.isEmpty())
//                    return@filter true
//
//                val validDirections = filterLines[it.lineId] ?: return@filter false
//
//                return@filter validDirections.contains(it.direction)
//            }
            .mapNotNull{ entry ->
                try {
                    Log.i("ARRIVAL-INFO", entry.expectedArrivalString!!)
                    Model(
                        operatorName = "TfL",
                        serviceName = processServiceName(entry.lineName!!),
                        destinationName = processDestinationName(entry.destinationName!!),
                        viaText = if (entry.towards!! != "null") entry.towards else "",
                        platformName = processPlatformName(entry.platformName!!),
                        remainingS = max(0, entry.timeToStation!! - 60),
                        expectedDateTime =
                            LocalDateTime
                                .from(responseTimeFormatter.parse(entry.expectedArrivalString))
                                .atOffset(ZoneOffset.UTC)
                                .atZoneSameInstant(ZoneId.systemDefault())
                                .toLocalDateTime()
                    )
                } catch (e: Exception) {
                    Log.e("ARRIVAL-INFO", e.message.toString())
                    null
                }
            }
            .sortedBy { model -> model.remainingS }
    }

    private val stripDestinationNameRegex = Regex("( Underground Station)|(, Bus Station)|( Station)|( DLR)")
    private fun processDestinationName(input: String): String {
        return stripDestinationNameRegex.replace(input, "")
    }

    private val stripPlatformDirectionRegex = Regex(".+?(?=Platform)|(Platform)|(\\s)")
    private fun processPlatformName(input: String): String {
        return stripPlatformDirectionRegex.replace(input, "")
    }

    private val stripServiceNameRegex = Regex("(Line)|[^A-Z0-9&]")
    private fun processServiceName(input: String): String {
        if (input.length <= 5)
            return input
        if (input.length <= 15)
            return input.take(5)
        else
            return stripServiceNameRegex.replace(input, "").take(5)
    }

}