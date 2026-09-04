package com.eden.livewidget.data.transitous.points.api

import android.content.Context
import android.util.Log
import com.eden.livewidget.data.common.points.Model
import com.eden.livewidget.data.common.points.Option
import com.eden.livewidget.data.common.points.api.DirectApi
import com.eden.livewidget.data.transitous.points.TransitousValue
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

private data class TransitousAutoComplete(
    @SerializedName("name")
    val name: String?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("areas")
    val areasNullable: List<TransitousArea>?,
) {
    fun isValid() =
        name != null &&
        id != null
}

private data class TransitousArea(
    @SerializedName("name")
    val name: String?,
) {
    fun isValid() =
        name != null
}

private const val BASE_URL = "https://api.transitous.org"

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

private interface PointsTransitousApiService {
    @Headers("User-Agent: TransportWidget/2.2.0+ (https://github.com/someone-s/transport-widget)")
    @GET("api/v1/geocode")
    fun getAutoComplete(
        @Query("type")
        type: String,
        @Query("text")
        text: String,
    ): Call<List<TransitousAutoComplete>>
}

class TransitousDirectApi: DirectApi {

    private val service: PointsTransitousApiService by lazy {
        retrofit.create(PointsTransitousApiService::class.java)
    }

    override suspend fun getAllFuzzyMatches(
        context: Context,
        input: String
    ): List<Option> {

        val pageRequest = service.getAutoComplete("STOP", input)
        pageRequest.request()

        val pageResponse: Response<List<TransitousAutoComplete>>
        try {
            pageResponse = pageRequest.execute()
        }
        catch (_: SocketTimeoutException) {
            Log.w(this.javaClass.name, "timeout for fetching auto complete")
            return emptyList()
        }

        if (pageResponse.body() !is List<TransitousAutoComplete>) {
            Log.i(this.javaClass.name, "failed to find page body")
            return emptyList()
        }

        val autoCompletes = pageResponse.body() as List<TransitousAutoComplete>

        return autoCompletes
            .filter { it.isValid() }
            .map {
                Option(
                    model =
                        Model(
                            name = it.name!!,
                            value = TransitousValue(it.id!!),
                        ),
                    annotation =
                        it.areasNullable
                        ?.filter { area -> area.isValid() }
                        ?.reversed()
                        ?.joinToString(", ") { area -> area.name!! }
                        ?: ""
                )
            }
    }
}