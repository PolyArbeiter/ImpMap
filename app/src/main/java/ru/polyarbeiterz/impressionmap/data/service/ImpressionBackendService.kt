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
        "Authorization: Basic dXNlcjE6dXNlcjEyMw=="
    ] )
    suspend fun getAllImpressions(): Response<List<ImpressionDto>>

    @GET("")
    @Headers( value = [
        "Accept-Encoding: gzip,deflate",
        "Content-Type: Image/Jpeg;charset=UTF-8",
        "Accept: Image/Jpeg",
        "User-Agent: Retrofit 2.9.0",
        "Authorization: Basic dXNlcjE6dXNlcjEyMw=="
    ] )
    suspend fun getImage(@Path("image_path") imagePath: String): Response<Any>

    @GET("")
    @Headers( value = [
        "Accept-Encoding: gzip,deflate",
        "Content-Type: Video/Mp4;charset=UTF-8",
        "Accept: Video/Mp4",
        "User-Agent: Retrofit 2.9.0",
        "Authorization: Basic dXNlcjE6dXNlcjEyMw=="
    ] )
    suspend fun getVideo(@Path("video_path") videoPath: String): Response<Any>

    @GET("api/v1/impressions/impression/{id}")
    suspend fun getImpressionById(@Path("id") id: Int): Response<ImpressionDto>

    @POST("api/v1/impressions/impressions/")
    @Headers( value = [
        "Accept-Encoding: gzip,deflate",
        "Content-Type: Application/Json;charset=UTF-8",
        "Accept: Application/Json",
        "User-Agent: Retrofit 2.9.0",
        "Authorization: Basic dXNlcjE6dXNlcjEyMw=="
    ] )
    suspend fun createImpression(@Body impressionDto: ImpressionDto): Response<ImpressionDto>
}