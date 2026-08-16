package com.eden.livewidget.data.tfl.filter.destination

import android.util.Log
import com.eden.livewidget.data.common.filter.destination.Compiler
import com.eden.livewidget.data.common.filter.destination.Filter
import com.eden.livewidget.data.common.filter.destination.Filter.Companion.cloneApplied
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.net.SocketTimeoutException

private data class TflStopPoint(
    @SerializedName("naptanId")
    val actualId: String?,
    @SerializedName("lineGroup")
    val lineGroups: List<TflStopPointLineGroup>?,
)

private data class TflStopPointLineGroup(
    @SerializedName("naptanIdReference")
    val id: String?,
    @SerializedName("lineIdentifier")
    val lineIds: List<String>?,
)
private const val BASE_URL = "https://api.tfl.gov.uk"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private interface TflApiService {
    @GET("StopPoint/{id}")
    fun getStopPoint(
        @Path("id")
        stopPointId: String,
    ): Call<TflStopPoint>

    @GET("StopPoint/{fromId}/DirectionTo/{toId}")
    fun getDirection(
        @Path("fromId")
        fromStopPointId: String,
        @Path("toId")
        toStopPointId: String,
        @Query("lineid")
        lineId: String,
    ): Call<String>
}

private val gson = Gson()

class TflCompiler: Compiler {

    private val service: TflApiService by lazy {
        retrofit.create(TflApiService::class.java)
    }

    override suspend fun compileFilter(filter: Filter): Filter {

        Log.i(this.javaClass.name, "Compiling filter")
        val fromStopPoints = filter.fromPoint.apiValue.split(',')
        val toStopPoints = filter.toPoint.apiValue.split(',')

        data class LineAndStop(
            val lineId: String,
            val stopPointId: String,
        )

        fun extractLines(stopPoints: List<String>) =
            stopPoints
                .mapNotNull { stopPointId ->

                    val infoRequest = service.getStopPoint(stopPointId)
                    infoRequest.request()

                    val infoResponse = try {
                        infoRequest.execute()
                    } catch (_: SocketTimeoutException) {
                        Log.w(this.javaClass.name, "timeout for stopPoint $stopPointId")
                        return@mapNotNull null
                    }

                    val infoBody = infoResponse.body()
                    if (infoBody !is TflStopPoint) {
                        Log.i(this.javaClass.name, "failed to find stopPoint $stopPointId body")
                        return@mapNotNull null
                    }

                    infoBody.lineGroups
                        ?.filter { it.id != null && it.lineIds != null }
                        ?.flatMap { group ->
                            group.lineIds!!.map { lineId ->
                                LineAndStop(
                                    lineId = lineId,
                                    stopPointId = group.id!!,
                                )
                            }
                        }
                }
                .flatten()
                .toSet()
                .groupBy { it.lineId }

        val fromLines = extractLines(fromStopPoints)
        val toLines = extractLines(toStopPoints)

        val linesWithDirection = fromLines
            .flatMap { fromLine ->
                val toIntermediates = toLines[fromLine.key] ?: return@flatMap emptySet<LineWithDirection>()
                val fromIntermediates = fromLine.value

                fromIntermediates
                    .flatMap { from ->
                        toIntermediates.map { to -> Pair(from.stopPointId, to.stopPointId) }
                    }
                    .mapNotNull { (fromStopPointId, toStopPointId) ->

                        val infoRequest = service.getDirection(
                            lineId = fromLine.key,
                            fromStopPointId = fromStopPointId,
                            toStopPointId = toStopPointId
                        )
                        infoRequest.request()

                        val infoResponse = try {
                            infoRequest.execute()
                        } catch (_: SocketTimeoutException) {
                            Log.w(this.javaClass.name, "$fromStopPointId-$toStopPointId direction timeout")
                            return@mapNotNull null
                        }

                        if (infoResponse.code() == 404) {
                            Log.i(this.javaClass.name, "$fromStopPointId-$toStopPointId not valid direction")
                            return@mapNotNull null
                        }

                        val infoBody = infoResponse.body()
                        if (infoBody !is String) {
                            Log.i(this.javaClass.name, "$fromStopPointId-$toStopPointId failed to find body")
                            return@mapNotNull null
                        }

                        val direction = gson.fromJson(infoBody, String::class.java)

                        Log.i(this.javaClass.name, "$fromStopPointId-$toStopPointId is valid direction")

                        LineWithDirection(
                            lineId = fromLine.key,
                            direction = direction,
                        )
                    }

            }
            .toSet()

        val filterValue = LineWithDirection.serializeList(linesWithDirection)


        return filter.cloneApplied(filterValue)
    }
}