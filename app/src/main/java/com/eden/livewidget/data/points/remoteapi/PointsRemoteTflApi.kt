package com.eden.livewidget.data.points.remoteapi

import android.content.Context
import android.util.Log
import com.eden.livewidget.R
import com.eden.livewidget.data.points.PointEntity
import com.eden.livewidget.data.points.PointsRemoteApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

private data class StopPoint(
    val id: String,
    val commonName: String,
    val indicator: String?,
)

private const val BASE_URL = "https://api.tfl.gov.uk"

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

private interface PointsTflApiService {
    @GET("StopPoint/Type/{types}/page/{page}")
    fun getPage(
        @Path("types") types: String,
        @Path("page") page: Int
    ): Call<List<StopPoint>>
}

class PointsRemoteTflApi(
    val ioDispatcher: CoroutineDispatcher
): PointsRemoteApi {

    private val fetchTypes = "NaptanFerryPort,NaptanPublicBusCoachTram,NaptanMetroStation"

    private val service: PointsTflApiService by lazy {
        retrofit.create(PointsTflApiService::class.java)
    }

    override suspend fun fetchPoints(
        context: Context,
        add: (PointEntity) -> Unit,
        statusUpdate: (String) -> Unit
    ) {

        val pageBatch = 10
        var pageSet = 0
        while(true) {

            var hasEmpty = false

            coroutineScope {
                for (i in 0..<pageBatch) {
                    launch {
                        withContext(ioDispatcher) {
                            if (fetchPage(pageSet * pageBatch + i, add) <= 0)
                                hasEmpty = true
                        }
                    }
                }

                statusUpdate(
                    context.getString(
                        R.string.provider_tfl_api_fetch_status_update_text,
                        pageSet * pageBatch,
                        pageSet * pageBatch + pageBatch - 1
                    ))
            }

            if (hasEmpty)
                break

            pageSet++
        }
    }

    private fun fetchPage(pageZeroIndexed: Int, add: (PointEntity) -> Unit): Int {

        val page = pageZeroIndexed + 1

        Log.i(this.javaClass.name, "request page $page")

        val pageRequest = service.getPage(fetchTypes, page)
        pageRequest.request()
        val pageResponse = pageRequest.execute()
        if (pageResponse == null) {
            Log.i(this.javaClass.name, "failed to fetch page $page")
            return -1
        } else if (pageResponse.body() !is List<StopPoint>) {
            Log.i(this.javaClass.name, "failed to find page body $page")
            return -1
        }
        val pageResult = pageResponse.body() as List<StopPoint>
        if (pageResult.isEmpty())
            return -1

        for (stopPoint in pageResult) {
            add(PointEntity(
                name = stopPoint.commonName + if (stopPoint.indicator != null) " (${stopPoint.indicator})" else "",
                apiValue = stopPoint.id
            ))
        }

        Log.i(this.javaClass.name, "added ${pageResult.size} entries from page $page")

        return pageResult.size

    }
}