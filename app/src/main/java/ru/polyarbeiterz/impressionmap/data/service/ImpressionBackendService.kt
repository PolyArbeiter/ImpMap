package ru.polyarbeiterz.impressionmap.data.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import ru.polyarbeiterz.impressionmap.data.dto.ImpressionDto


interface ImpressionBackendService {

    @GET("api/v1/impressions/impressions/")
    @Headers( value = [
        "Accept-Encoding: gzip,deflate",
        "Content-Type: Application/Json;charset=UTF-8",
        "Accept: Application/Json",
        "User-Agent: Retrofit 2.9.0",
        "Authorization: Basic bWF0dmV5OjE="
    ] )
    suspend fun getAllImpressions(): Response<List<ImpressionDto>>

    @GET("api/v1/impressions/impression/{id}")
    suspend fun getImpressionById(@Path("id") id: Int): Response<ImpressionDto>

    @POST("api/v1/impressions/impressions/")
    @Headers( value = [
        "Accept-Encoding: gzip,deflate",
        "Content-Type: Application/Json;charset=UTF-8",
        "Accept: Application/Json",
        "User-Agent: Retrofit 2.9.0",
        "Authorization: Basic bWF0dmV5OjE="
    ] )
    suspend fun createImpression(@Body impressionDto: ImpressionDto): Response<ImpressionDto>
}