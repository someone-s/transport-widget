package com.eden.livewidget.data.points.remoteapi

import android.util.Log
import com.eden.livewidget.data.points.PointModel
import com.eden.livewidget.data.points.PointsRemoteApi
import com.eden.livewidget.data.utils.Provider
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private data class StopPointResponse(
    val id: String,
//    val url: String,
    val commonName: String,
    val stopLetter: String?,
    val children: List<StopPointResponse>, // recursive
    val stopType: String // i,e. "NaptanBusCoachStation" "NaptanMetroStation"
)

private data class Match(
    val id: String,
//    val url: String,
    val name: String,
//    val lat: String,
//    val lon: String,
)

private data class SearchResponse(
//    val query: String,
//    val from: Int,
//    val page: Int,
//    val pageSize: Int,
//    val provider: String,
//    val total: Int,
    val matches: List<Match>,
//    val maxScore: Int,
)

private const val BASE_URL = "https://api.tfl.gov.uk"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private interface PointsTflApiService {
    @GET("StopPoint/Search")
    fun getMatching(
        @Query("query")
        input: String,
    ): Call<SearchResponse>


    @GET("StopPoint/{ids}")
    fun getActual(
        @Path("ids")
        id: String,
    ): Call<StopPointResponse>
}

class PointsRemoteTflApi: PointsRemoteApi {

    private val service: PointsTflApiService by lazy {
        retrofit.create(PointsTflApiService::class.java)
    }

    override fun fetchMatching(input: String): List<PointModel> {
        Log.i(this.javaClass.name, "Data fetched")
        val searchRequest = service.getMatching(input)
        val searchResponse = searchRequest.execute()
        if (searchResponse == null) {
            Log.i(this.javaClass.name, "no response")
            return emptyList()
        } else if (searchResponse.body() !is SearchResponse) {
            Log.i(this.javaClass.name, "no search")
            return emptyList()
        }

        val matches = (searchResponse.body() as SearchResponse).matches

        val outputs = mutableListOf<PointModel>()
        for (match in matches) {
            val actualRequest = service.getActual(match.id)
            val actualResponse = actualRequest.execute()
            if (actualResponse == null) {
                Log.i(this.javaClass.name, "no response 2")
                continue
            } else if (actualResponse.body() !is StopPointResponse) {
                Log.i(this.javaClass.name, "no actual")
                continue
            }

            traverseStopPoint(actualResponse.body() as StopPointResponse, outputs)
        }

        return outputs

    }

    private fun traverseStopPoint(stopPoint: StopPointResponse, outputs: MutableList<PointModel>) {
       Log.i("AAAA", stopPoint.stopType)
        when (stopPoint.stopType) {
            "NaptanMetroStation" -> {
                outputs.add(
                    PointModel(
                        name = stopPoint.commonName,
                        apiProvider = Provider.TFL,
                        apiValue = stopPoint.id,
                        context = null,
                    )
                )
            }
            "NaptanPublicBusCoachTram" -> {
                outputs.add(
                    PointModel(
                        name = stopPoint.commonName + if (stopPoint.stopLetter != null) " (${stopPoint.stopLetter})" else "",
                        apiProvider = Provider.TFL,
                        apiValue = stopPoint.id,
                        context = null,
                    )
                )
            }
            else -> {
                for (child in stopPoint.children) {
                    traverseStopPoint(child, outputs)
                }
            }
        }
    }
}