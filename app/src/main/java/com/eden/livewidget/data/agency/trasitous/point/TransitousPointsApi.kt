package com.eden.livewidget.data.agency.trasitous.point

import android.util.Log
import com.eden.livewidget.data.agency.common.point.FixedLogicalPoint
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

private data class TransitousLocation(
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
    fun getGeocode(
        @Query("type")
        type: String,
        @Query("text")
        text: String,
    ): Call<List<TransitousLocation>>

    @Headers("User-Agent: TransportWidget/2.2.0+ (https://github.com/someone-s/transport-widget)")
    @GET("api/v1/reverse-geocode")
    fun getReverseGeocode(
        @Query("type")
        type: String,
        @Query("place")
        place: String,
        @Query("numResults")
        count: Int,
    ): Call<List<TransitousLocation>>
}

class TransitousPointsApi {

    data class Option(
        val logicalPoint: FixedLogicalPoint,
        val annotation: String? = null,
    )

    private val service: PointsTransitousApiService by lazy {
        retrofit.create(PointsTransitousApiService::class.java)
    }

    fun getAllFuzzyMatches(
        input: String
    ): List<Option> {

        val request = service.getGeocode("STOP", input)
        request.request()

        val response = try {
            request.execute()
        } catch (_: SocketTimeoutException) {
            Log.w(this.javaClass.name, "timeout for fetching auto complete")
            return emptyList()
        }

        if (response.body() !is List<TransitousLocation>) {
            Log.i(this.javaClass.name, "failed to find page body")
            return emptyList()
        }

        val locations = response.body() as List<TransitousLocation>

        return locations
            .filter { it.isValid() }
            .map {
                Option(
                    logicalPoint =
                        FixedLogicalPoint(
                            nameString = it.name!!,
                            dataPoint = TransitousDataPoint(it.id!!),
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

    @Throws(
        UnreachableException::class,
        UnresolvedException::class,
        SchemaException::class,
        NoResultException::class
    )
    fun getClosest(latitude: Double, longitude: Double): FixedLogicalPoint {

        val request = service.getReverseGeocode("STOP", "$latitude,$longitude", 1)
        request.request()

        val response = try {
            request.execute()
        } catch (e: IOException) {
            Log.w(this.javaClass.name, e.message ?: "mo error message")
            throw UnresolvedException("Unable to reach server")
        }

        if (response == null) {
            Log.w(this.javaClass.name, "no response")
            throw UnreachableException("Response empty")
        }

        if (response.body() !is List<TransitousLocation>) {
            Log.i(this.javaClass.name, "failed to find page body")
            throw SchemaException("Unexpected response body")
        }

        val locations = response.body() as List<TransitousLocation>
        if (locations.isEmpty()) throw NoResultException("No result")

        return FixedLogicalPoint(
            nameString = locations[0].name!!,
            dataPoint = TransitousDataPoint(locations[0].id!!),
        )
    }

    class UnresolvedException(message: String?) : RuntimeException(message)
    class UnreachableException(message: String?) : RuntimeException(message)
    class SchemaException(message: String?): RuntimeException(message)
    class NoResultException(message: String?): RuntimeException(message)

}