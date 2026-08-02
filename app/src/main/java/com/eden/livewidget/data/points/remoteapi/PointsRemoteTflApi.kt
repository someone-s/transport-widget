package com.eden.livewidget.data.points.remoteapi

import android.content.Context
import android.util.Log
import com.eden.livewidget.R
import com.eden.livewidget.data.points.PointEntity
import com.eden.livewidget.data.points.PointsRemoteApi
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val naptanId: String,
    @SerializedName("modes")
    val modes: List<String>?,
    @SerializedName("stopType")
    val stopType: String,
    @SerializedName("commonName")
    val commonName: String,
    @SerializedName("children")
    val children: List<TflStopPoint>,
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
        @Path("types") commaSeparatedTypes: String,
        @Path("page") page: Int
    ): Call<List<TflStopPoint>>
}

class PointsRemoteTflApi(
    val ioDispatcher: CoroutineDispatcher
): PointsRemoteApi {

    private val fetchStopTypes = setOf(
        "NaptanFerryPort",
        "NaptanOnstreetBusCoachStopPair",
        "NaptanOnstreetBusCoachStopCluster",
        "NaptanBusCoachStation",
        "NaptanMetroStation"
    )
    private val validModes = mapOf(
        "bus" to 1,
        "river-bus" to 2,
        "dlr" to 4,
        "tram" to 8,
        "tube" to 16
    )
    private val busModeMask =
        validModes["bus"]!!
    private val busChildStopType =
        "NaptanPublicBusCoachTram"
    private val directModeMask =
        validModes["river-bus"]!! +
        validModes["dlr"]!! +
        validModes["tram"]!! +
        validModes["tube"]!!

    private val service: PointsTflApiService by lazy {
        retrofit.create(PointsTflApiService::class.java)
    }

    override suspend fun fetchPoints(
        context: Context,
        outputPoint: (PointEntity) -> Unit,
        statusUpdate: (String) -> Unit
    ) {

        val pageBatch = 3
        val maxAttempt = 3
        var pageSet = 0
        while(true) {

            var hasEmpty = false

            coroutineScope {
                fun startFetch(page: Int, tryCount: Int) {
                    if (tryCount >= maxAttempt) {
                        Log.e(this.javaClass.name, "failed to fetch page $page despite retry")
                    }
                    launch {
                        withContext(ioDispatcher) {
                            fetchPage(
                                pageZeroIndexed = page,
                                outputPoint = outputPoint,
                                onSuccess = { count -> if (count == 0) hasEmpty = true },
                                onFailure = { startFetch(page, tryCount + 1) }
                            )
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

    private fun fetchPage(pageZeroIndexed: Int, outputPoint: (PointEntity) -> Unit, onSuccess: (Int) -> Unit, onFailure: () -> Unit) {

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
            onFailure()
            return
        }

        if (pageResponse.body() !is List<TflStopPoint>) {
            Log.i(this.javaClass.name, "failed to find page body $page")
            onFailure()
            return
        }
        val stopPoints = pageResponse.body() as List<TflStopPoint>
        if (stopPoints.isEmpty()) {
            onSuccess(0)
            return
        }

        stopPoints
            .filter { !it.modes.isNullOrEmpty() }
            .forEach { stopPoint ->

                fun constructMask(): Int {
                    var temp = 0
                    stopPoint.modes!!.forEach { mode -> temp += validModes.getOrDefault(mode, 0) }
                    return temp
                }
                val modeMask = constructMask()

                val arrivalNaptanIds = buildSet {
                    if ((modeMask and busModeMask) > 0)
                        stopPoint.children
                            .filter { child -> child.stopType == busChildStopType }
                            .forEach { child -> add(child.naptanId) }
                    if ((modeMask and directModeMask) > 0)
                        add(stopPoint.naptanId)
                }

                outputPoint(
                    PointEntity(
                        name = stopPoint.commonName,
                        apiValue = arrivalNaptanIds.joinToString(",")
                    )
                )
            }

        Log.i(this.javaClass.name, "processed ${stopPoints.size} entries from page $page")

        onSuccess(stopPoints.size)
        return

    }
}