package com.eden.livewidget.api

import android.util.Log
import com.eden.livewidget.data.ArrivalModel
import com.eden.livewidget.data.LivePointApi
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private data class ArrivalTiming(
    val countdownServerAdjustment: String,
    val source: String,
    val insert: String,
    val read: String,
    val sent: String,
    val received: String
)

private data class ArrivalEntry(
    val id: String,
    val operationType: Int,
    val vehicleId: String,
    val naptanId: String,
    val stationName: String,
    val lineId: String,
    val lineName: String,
    val platformName: String,
    val direction: String,
    val bearing: String,
    val destinationNaptanId: String,
    val destinationName: String,
    val timestamp: String,
    val timeToStation: Int,
    val currentLocation: String,
    val towards: String,
    val expectedArrival: String,
    val timeToLive: String,
    val modeName: String,
    val timing: ArrivalTiming
)

private const val BASE_URL = "https://api.tfl.gov.uk"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private interface LivePointTflApiService {
    @GET("StopPoint/{id}/Arrivals")
    fun getStopPointArrivals(
        @Path("id")
        stopPointId: String
    ): Call<List<ArrivalEntry>>
}
class LivePointTflApi(
    private val stopPointId: String
): LivePointApi {

    private val service: LivePointTflApiService by lazy {
        retrofit.create(LivePointTflApiService::class.java)
    }

    override fun fetchLatestArrivals(): List<ArrivalModel> {

        Log.i(this.javaClass.name, "Data fetched")
        val request = service.getStopPointArrivals(stopPointId)
        val response = request.execute()
        if (response == null) {
            Log.i(this.javaClass.name, "no response")
            return emptyList()
        }
        else if (response.body() !is List<ArrivalEntry>) {
            Log.i(this.javaClass.name, "no body")
            return emptyList()
        }
        else {
            for (l in response.body() as List<ArrivalEntry>)
                Log.i(this.javaClass.name, l.expectedArrival)
        }
        return emptyList()
    }
}