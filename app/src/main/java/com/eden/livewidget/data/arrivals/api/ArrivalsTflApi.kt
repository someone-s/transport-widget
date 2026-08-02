package com.eden.livewidget.data.arrivals.api

import android.content.Context
import android.util.Log
import com.eden.livewidget.data.arrivals.ArrivalModel
import com.eden.livewidget.data.arrivals.ArrivalsApi
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import kotlin.math.max

private data class TflArrivalEntry(
    @SerializedName("lineName")
    val lineName: String,
    @SerializedName("platformName")
    val platformName: String,
    @SerializedName("destinationName")
    val destinationName: String,
    @SerializedName("towards")
    val towards: String,
    @SerializedName("timeToStation")
    val timeToStation: Int,
)

private const val BASE_URL = "https://api.tfl.gov.uk"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private interface ArrivalsTflApiService {
    @GET("StopPoint/{id}/Arrivals")
    fun getStopPointArrivals(
        @Path("id")
        stopPointId: String,
    ): Call<List<TflArrivalEntry>>
}

class ArrivalsTflApi(
    commaSeparatedNaptanIds: String,
) : ArrivalsApi {

    private val naptanIds: List<String> = commaSeparatedNaptanIds.split(",")

    private val service: ArrivalsTflApiService by lazy {
        retrofit.create(ArrivalsTflApiService::class.java)
    }

    override suspend fun fetchLatestArrivals(context: Context): List<ArrivalModel> {

        val entries = mutableListOf<TflArrivalEntry>()

        coroutineScope {
            naptanIds.forEach { naptanId ->
                launch {
                    Log.i(this.javaClass.name, "Fetching data for $naptanId")
                    val request = service.getStopPointArrivals(naptanId)

                    val response = request.execute()
                    if (response == null) {
                        Log.i(this.javaClass.name, "no response")
                        return@launch
                    }

                    val body = response.body()
                    if (body !is List<TflArrivalEntry>) {
                        Log.i(this.javaClass.name, "no body")
                        return@launch
                    }

                    entries.addAll(body)
                }
            }
        }

        entries.sortBy { entry -> entry.timeToStation }


        return entries
            .map { entry ->
                Log.i("ARRIVAL-INFO", entry.timeToStation.toString())
                ArrivalModel(
                    operatorName = "Transport for London",
                    serviceName = entry.lineName,
                    destinationName = entry.destinationName,
                    viaText = entry.towards,
                    platformName = entry.platformName,
                    remainingS = max(0, entry.timeToStation - 60)
                )
            }
            .sortedBy { model -> model.remainingS }
    }
}