package com.eden.livewidget.data.tfl.arrivals.filter.destination

import android.util.Log
import com.eden.livewidget.data.common.arrivals.filter.destination.Compiler
import com.eden.livewidget.data.common.arrivals.filter.destination.Filter
import com.eden.livewidget.data.common.arrivals.filter.destination.Filter.Companion.cloneApplied
import com.eden.livewidget.data.tfl.points.TflValue as PointsTflValue
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.net.SocketTimeoutException

private const val BASE_URL = "https://api.tfl.gov.uk"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private interface TflApiService {

    /**
     * The return value is a JSON string, i.e. "inbound",
     * where double quotes are included
     */
    @GET("StopPoint/{fromId}/DirectionTo/{toId}")
    fun getDirection(
        @Path("fromId")
        fromNaptanId: String,
        @Path("toId")
        toNaptanId: String,
        @Query("lineid")
        lineId: String,
    ): Call<String>
}

class TflCompiler: Compiler {

    private val service: TflApiService by lazy {
        retrofit.create(TflApiService::class.java)
    }

    override suspend fun compileFilter(filter: Filter): Filter {

        Log.i(this.javaClass.name, "Compiling filter")
        val fromPoints = filter.fromPoint.values.map { value -> (value as PointsTflValue) }
        val toPoints = filter.toPoint.values.map { value -> (value as PointsTflValue) }

        data class LineAndStop(
            val lineId: String,
            val naptanId: String,
        )

        fun extractLines(points: List<PointsTflValue>) =
            points
                .flatMap { point ->
                    point.lineIds.map {
                        LineAndStop(
                            lineId = it,
                            naptanId = point.naptanId,
                        )
                    }
                }
                .toSet()
                .groupBy { it.lineId }

        val fromLines = extractLines(fromPoints)
        val toLines = extractLines(toPoints)

        val values = fromLines
            .flatMap { line ->

                val lineId = line.key

                // Check if line is in both from and to sets
                val toIntermediates = toLines[lineId] ?: return@flatMap emptySet<TflValue>()
                val fromIntermediates = line.value

                fromIntermediates
                    .flatMap { from ->
                        toIntermediates.map { to -> Pair(from.naptanId, to.naptanId) }
                    }
                    .mapNotNull { (fromNaptanId, toNaptanId) ->

                        val infoRequest = service.getDirection(
                            lineId = lineId,
                            fromNaptanId = fromNaptanId,
                            toNaptanId = toNaptanId
                        )
                        infoRequest.request()

                        val infoResponse = try {
                            infoRequest.execute()
                        } catch (_: SocketTimeoutException) {
                            Log.w(this.javaClass.name, "$fromNaptanId-$toNaptanId direction timeout")
                            return@mapNotNull null
                        }

                        if (infoResponse.code() == 404) {
                            Log.i(this.javaClass.name, "$fromNaptanId-$toNaptanId not valid direction")
                            return@mapNotNull null
                        }

                        val direction = infoResponse.body()
                        if (direction !is String) {
                            Log.i(this.javaClass.name, "$fromNaptanId-$toNaptanId failed to find body")
                            return@mapNotNull null
                        }

                        Log.i(this.javaClass.name, "$fromNaptanId-$toNaptanId has direction $direction")

                        TflValue(
                            lineId = lineId,
                            fromNaptanId = fromNaptanId,
                            direction = direction,
                        )
                    }

            }
            .distinct()

        return filter.cloneApplied(values)
    }
}