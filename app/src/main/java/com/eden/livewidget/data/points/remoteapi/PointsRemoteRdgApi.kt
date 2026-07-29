package com.eden.livewidget.data.points.remoteapi

import android.content.Context
import android.util.Log
import com.eden.livewidget.R
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.keys.KeyPurpose
import com.eden.livewidget.data.points.PointEntity
import com.eden.livewidget.data.points.PointsRemoteApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import java.util.concurrent.TimeUnit

private data class RdgStationListResponse(
    @SerialName("StationList")
    val stationList: List<RdgStation>
)

private data class RdgStation(
    @SerialName("crs")
    val crsCode: String,
    @SerialName("Value")
    val commonName: String
)

private const val BASE_URL = "https://api1.raildata.org.uk"

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

private interface PointsRdgApiService {
    @GET("1010-reference-data1_0/LDBSVWS/api/ref/20211101/GetStationList/1")
    fun getStationList(
        @HeaderMap headers: Map<String, String>
    ): Call<RdgStationListResponse>
}

class PointsRemoteRdgApi(
    val ioDispatcher: CoroutineDispatcher,
    val apiProvider: Provider
): PointsRemoteApi {

    private val service: PointsRdgApiService by lazy {
        retrofit.create(PointsRdgApiService::class.java)
    }

    override suspend fun fetchPoints(
        context: Context,
        add: (PointEntity) -> Unit,
        statusUpdate: (String) -> Unit
    ) {
        coroutineScope {
            launch {
                withContext(ioDispatcher) {
                    fetchData(context, add)
                }
            }

            statusUpdate(
                context.getString(R.string.provider_rdg_api_fetch_status_update_text)
            )
        }
    }

    private fun fetchData(
        context: Context,
        add: (PointEntity) -> Unit
    ) {
        Log.i(this.javaClass.name, "request data")

        val headers = mapOf("x-apikey" to apiProvider.keyProviders[KeyPurpose.POINTS]!!.getKey(context))
        val pageRequest = service.getStationList(headers)
        pageRequest.request()

        val response = pageRequest.execute()
        if (response == null) {
            Log.i(this.javaClass.name, "failed to fetch data")
            return
        } else if (response.body() !is RdgStationListResponse) {
            Log.i(this.javaClass.name, "failed to find data body")
            return
        }
        val stationList = (response.body() as RdgStationListResponse).stationList
        if (stationList.isEmpty())
            return

        for (station in stationList) {
            add(PointEntity(
                name = station.commonName,
                apiValue = station.crsCode
            ))
        }

        Log.i(this.javaClass.name, "added ${stationList.size} entries")
    }
}