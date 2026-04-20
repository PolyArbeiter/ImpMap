package ru.polyarbeiterz.impressionmap.data.service

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    @GET("{image_path}")
    @Headers( value = [
        "Content-Type: image/jpeg;charset=UTF-8",
        "User-Agent: Retrofit 2.9.0",
    ] )
    suspend fun getImage(@Path("image_path") imagePath: String): Response<ResponseBody>

    @GET("{video_path}")
    @Headers( value = [
        "Content-Type: video/mp4;charset=UTF-8",
        "User-Agent: Retrofit 2.9.0",
    ] )
    suspend fun getVideo(@Path("video_path") videoPath: String): Response<ResponseBody>


    @Multipart
    @POST("api/v1/impressions/impressions/")
    suspend fun createImpression(
        @Part("local_id") localId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("date") date: RequestBody,
        @Part media: List<MultipartBody.Part>
    ): Response<ResponseBody>

    @POST("api/v1/auth/login/")
    @Headers( value = [
        "Accept-Encoding: gzip,deflate",
        "Content-Type: Application/Json;charset=UTF-8",
        "Accept: Application/Json",
        "User-Agent: Retrofit 2.9.0"
    ] )
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}