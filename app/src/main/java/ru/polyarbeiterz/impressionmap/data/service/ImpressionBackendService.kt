package ru.polyarbeiterz.impressionmap.data.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import ru.polyarbeiterz.impressionmap.data.dto.ImpressionResponse
import ru.polyarbeiterz.impressionmap.data.dto.LoginRequest
import ru.polyarbeiterz.impressionmap.data.dto.LoginResponse


interface ImpressionBackendService {

    @GET("api/v1/impressions/impressions/")
    @Headers( value = [
        "Accept-Encoding: gzip,deflate",
        "Content-Type: Application/Json;charset=UTF-8",
        "Accept: Application/Json",
        "User-Agent: Retrofit 2.9.0",
    ] )
    suspend fun getAllImpressions(): Response<List<ImpressionResponse>>

    @GET("")
    @Headers( value = [
        "Accept-Encoding: gzip,deflate",
        "Content-Type: Image/Jpeg;charset=UTF-8",
        "Accept: Image/Jpeg",
        "User-Agent: Retrofit 2.9.0",
    ] )
    suspend fun getImage(@Path("image_path") imagePath: String): Response<Any>

    @GET("")
    @Headers( value = [
        "Accept-Encoding: gzip,deflate",
        "Content-Type: Video/Mp4;charset=UTF-8",
        "Accept: Video/Mp4",
        "User-Agent: Retrofit 2.9.0",
    ] )
    suspend fun getVideo(@Path("video_path") videoPath: String): Response<Any>

    @GET("api/v1/impressions/impression/{id}")
    suspend fun getImpressionById(@Path("id") id: Int): Response<ImpressionResponse>

    @POST("api/v1/impressions/impressions/")
    @Headers( value = [
        "Accept-Encoding: gzip,deflate",
        "Content-Type: Application/Json;charset=UTF-8",
        "Accept: Application/Json",
        "User-Agent: Retrofit 2.9.0",
    ] )
    suspend fun createImpression(@Body impressionResponse: ImpressionResponse): Response<ImpressionResponse>

    @POST("api/v1/auth/login/")
    @Headers( value = [
        "Accept-Encoding: gzip,deflate",
        "Content-Type: Application/Json;charset=UTF-8",
        "Accept: Application/Json",
        "User-Agent: Retrofit 2.9.0"
    ] )
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}