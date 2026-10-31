package com.eden.livewidget.data.tfl

import android.util.Log
import com.eden.livewidget.data.common.DestinationCompiler
import com.eden.livewidget.data.common.DestinationConfig
import com.eden.livewidget.data.common.DestinationFilter
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.net.SocketTimeoutException


class TflDestinationCompiler: DestinationCompiler {

    private data class TflLine(
        @SerializedName("orderedLineRoutes")
        val routes: List<TflRoute>?,
    ) {
        fun isValid() =
            routes != null
    }

    private data class TflRoute(
        @SerializedName("naptanIds")
        val naptanIds: List<String>?,
    ) {
        fun isValid() =
            naptanIds != null
    }

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.tfl.gov.uk")
        .build()

    private interface TflDestinationFilterApiService {

        /**
         * The return value is a JSON string, i.e. "inbound",
         * Gson converter automatically remove the JSON double quotes
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

        @GET("Line/{lineId}/Route/Sequence/{direction}")
        fun getLine(
            @Path("lineId")
            lineId: String,
            @Path("direction")
            direction: String,
        ): Call<TflLine>
    }

    private val service: TflDestinationFilterApiService by lazy {
        retrofit.create(TflDestinationFilterApiService::class.java)
    }

    override suspend fun compileFilter(filter: DestinationConfig): List<DestinationFilter> {

        Log.i(this.javaClass.name, "Compiling filter")
        val fromPoints = filter.fromPoint.dataPoints.map { value -> (value as TflDataPoint) }
        val toPoints = filter.toPoint.dataPoints.map { value -> (value as TflDataPoint) }

        data class LineAndStop(
            val lineId: String,
            val naptanId: String,
        )

        fun extractLines(points: List<TflDataPoint>) =
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
                val toIntermediates = toLines[lineId] ?: return@flatMap emptySet<TflDestinationFilter>()
                val fromIntermediates = line.value

                fromIntermediates
                    .flatMap { from ->
                        toIntermediates.map { to -> Pair(from.naptanId, to.naptanId) }
                    }
                    .mapNotNull { (fromNaptanId, toNaptanId) ->

                        val direction = fetchDirection(
                            lineId = lineId,
                            fromNaptanId = fromNaptanId,
                            toNaptanId = toNaptanId,
                        ) ?: return@mapNotNull null

                        Log.i(this.javaClass.name, "$fromNaptanId-$toNaptanId has direction $direction")

                        val routes = fetchRoutes(
                            lineId = lineId,
                            direction = direction,
                        ) ?: return@mapNotNull null

                        for (route in routes)
                            Log.i(this.javaClass.name, "$fromNaptanId-$toNaptanId found route $route")

                        val finalNaptanIds = routes
                            .filter { it.isValid() }
                            .flatMap {
                                checkNotNull(it.naptanIds)

                                val fromIndex = it.naptanIds.indexOf(fromNaptanId)
                                val toIndex = it.naptanIds.indexOf(toNaptanId)

                                if (fromIndex >= 0 && toIndex >= 0 && fromIndex < toIndex)
                                    it.naptanIds.subList(toIndex, it.naptanIds.lastIndex)
                                else
                                    emptyList()
                            }

                        for (finalNaptanId in finalNaptanIds)
                            Log.i(this.javaClass.name, "$fromNaptanId-$toNaptanId found valid final $finalNaptanId")


                        TflDestinationFilter(
                            lineId = lineId,
                            fromNaptanId = fromNaptanId,
                            direction = direction,
                            finalNaptanIds = finalNaptanIds,
                        )
                    }

            }
            .distinct()

        return values
    }

    private fun fetchDirection(
        lineId: String,
        fromNaptanId: String,
        toNaptanId: String,
    ): String? {

        val infoRequest = service.getDirection(
            lineId = lineId,
            fromNaptanId = fromNaptanId,
            toNaptanId = toNaptanId,
        )
        infoRequest.request()

        val infoResponse = try {
            infoRequest.execute()
        } catch (_: SocketTimeoutException) {
            Log.w(this.javaClass.name, "$fromNaptanId-$toNaptanId direction timeout")
            return null
        }

        if (infoResponse.code() == 404) {
            Log.i(this.javaClass.name, "$fromNaptanId-$toNaptanId not valid direction")
            return null
        }

        val direction = infoResponse.body()
        if (direction !is String) {
            Log.i(this.javaClass.name, "$fromNaptanId-$toNaptanId failed to find body")
            return null
        }

        return direction
    }


    private fun fetchRoutes(
        lineId: String,
        direction: String,
    ): List<TflRoute>? {

        val infoRequest = service.getLine(
            lineId = lineId,
            direction = direction,
        )
        infoRequest.request()

        val infoResponse = try {
            infoRequest.execute()
        } catch (_: SocketTimeoutException) {
            Log.w(this.javaClass.name, "$lineId $direction direction timeout")
            return null
        }

        val line = infoResponse.body()
        if (line == null) {
            Log.i(this.javaClass.name, "$lineId $direction failed to find body")
            return null
        }

        if (!line.isValid()) {
            Log.i(this.javaClass.name, "$lineId $direction body not valid")
            return null
        }

        return line.routes!!
    }


}