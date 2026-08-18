package com.eden.livewidget.data.tfl.points.api

import android.content.Context
import android.util.Log
import com.eden.livewidget.R
import com.eden.livewidget.data.common.points.Model
import com.eden.livewidget.data.common.points.api.Api
import com.eden.livewidget.data.tfl.points.TflValue
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

private data class TflStopPoint(
    @SerializedName("naptanId")
    val naptanId: String?,
    @SerializedName("modes")
    val modes: List<String>?,
    @SerializedName("commonName")
    val commonName: String?,
    @SerializedName("lines")
    val lines: List<TflLine>?,
) {
    fun isValid() =
        naptanId != null &&
        modes != null &&
        commonName != null &&
        lines != null && lines.all { it.isValid() }
}

private data class TflLine(
    @SerializedName("id")
    val id: String?,
) {
    fun isValid() =
        id != null
}

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
        @Path("types") commaSeparatedTypes: String,
        @Path("page") page: Int
    ): Call<List<TflStopPoint>>
}

class TflApi: Api {

    private val fetchStopTypes = setOf(
        "NaptanFerryPort",
        "NaptanPublicBusCoachTram",
        "NaptanMetroStation"
    )
    private val validModes = setOf(
        "bus",
        "river-bus",
        "dlr",
        "tram",
        "tube",
    )

    private val service: PointsTflApiService by lazy {
        retrofit.create(PointsTflApiService::class.java)
    }

    override suspend fun fetchPoints(
        context: Context,
        statusUpdate: (String) -> Unit,
    ): Flow<Model> = channelFlow {

        val pageBatch = 3
        val maxAttempt = 3
        var pageSet = 0
        while(true) {

            var hasEmpty = false

            coroutineScope {
                fun startFetch(page: Int, tryCount: Int) {
                    if (tryCount >= maxAttempt) {
                        Log.e(this.javaClass.name, "failed to fetch page $page despite retry $tryCount")
                    }

                    launch {
                        val result = fetchPage(page)

                        if (result.success) {
                            checkNotNull(result.values)

                            Log.i(this.javaClass.name, "submitting ${result.values.size} entries from page $page")

                            if (result.unfilteredIsEmpty)
                                hasEmpty = true

                            for (value in result.values)
                                send(value)
                        } else {
                            startFetch(page, tryCount + 1)
                        }
                    }
                }

                for (i in 0..<pageBatch) {
                    startFetch(pageSet * pageBatch + i, 0)
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

    private data class PageResult(
        val success: Boolean,
        val unfilteredIsEmpty: Boolean = false,
        val values: List<Model>? = null,
    )
    private fun fetchPage(
        pageZeroIndexed: Int,
    ): PageResult {

        val page = pageZeroIndexed + 1

        Log.i(this.javaClass.name, "request page $page")

        val pageRequest = service.getPage(fetchStopTypes.joinToString(","), page)
        pageRequest.request()

        val pageResponse: Response<List<TflStopPoint>>
        try {
            pageResponse = pageRequest.execute()
        }
        catch (_: SocketTimeoutException) {
            Log.w(this.javaClass.name, "timeout for page $page")
            return PageResult(success = false)
        }

        if (pageResponse.body() !is List<TflStopPoint>) {
            Log.i(this.javaClass.name, "failed to find page body $page")
            return PageResult(success = false)
        }
        val stopPoints = pageResponse.body() as List<TflStopPoint>

        Log.i(this.javaClass.name, "found ${stopPoints.size} entries from page $page")

        return PageResult(
            success = true,
            unfilteredIsEmpty = stopPoints.isEmpty(),
            values = stopPoints
                .filter { it.isValid() }
                .filter { it.modes!!.any { mode -> validModes.contains(mode) } }
                .map { stopPoint ->
                    Model(
                        name = stopPoint.commonName!!,
                        value = TflValue(
                            naptanId = stopPoint.naptanId!!,
                            lineIds = stopPoint.lines!!.map { it.id!! },
                        ),
                    )
                }
        )

    }
}