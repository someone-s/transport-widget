package com.eden.livewidget.data.rdg.points.api

import android.content.Context
import android.util.Log
import com.eden.livewidget.R
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.common.keys.KeyPurpose
import com.eden.livewidget.data.common.keys.getKeyProviderConstructor
import com.eden.livewidget.data.common.points.PointModel
import com.eden.livewidget.data.common.points.api.Api
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import java.util.concurrent.TimeUnit

private data class RdgStationListResponse(
    @SerializedName("StationList")
    val stationList: List<RdgStation>?
)

private data class RdgStation(
    @SerializedName("crs")
    val crsCode: String,
    @SerializedName("Value")
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

class RdgApi(
    val apiProvider: Provider
): Api {

    private val service: PointsRdgApiService by lazy {
        retrofit.create(PointsRdgApiService::class.java)
    }

    override suspend fun fetchPoints(
        context: Context,
        statusUpdate: (String) -> Unit
    ): Flow<PointModel> = flow {

        val points = fetchData(context)
        for (point in points)
            emit(point)

        statusUpdate(
            context.getString(R.string.provider_rdg_api_fetch_status_update_text)
        )
    }

    private fun fetchData(
        context: Context,
    ): List<PointModel> {
        Log.i(this.javaClass.name, "request data")

        val headers = mapOf(
            "x-apikey" to apiProvider
                .keyProviders.getKeyProviderConstructor(KeyPurpose.POINTS)!!()
                .getKey(context)
        )
        val pageRequest = service.getStationList(headers)
        pageRequest.request()

        val response = pageRequest.execute()
        if (response == null) {
            Log.i(this.javaClass.name, "failed to fetch data")
            return emptyList()
        }
        if (response.body() !is RdgStationListResponse) {
            Log.i(this.javaClass.name, "failed to find data body")
            return emptyList()
        }
        val body = response.body()
        if (body == null) {
            Log.i(this.javaClass.name, "failed to read data body")
            return emptyList()
        }
        val stationList = body.stationList
        if (stationList.isNullOrEmpty()) {
            Log.i(this.javaClass.name, "failed to find station list")
            return emptyList()
        }

        Log.i(this.javaClass.name, "found ${stationList.size} entries")

        return stationList.map {
            PointModel(
                name = it.commonName,
                apiValue = it.crsCode
            )
        }
    }
}