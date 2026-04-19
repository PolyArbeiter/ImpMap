package ru.polyarbeiterz.impressionmap.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.polyarbeiterz.impressionmap.data.service.ImpressionBackendService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UrlManager @Inject constructor() {
    private val _baseUrl = MutableStateFlow("http://127.0.0.1:8000")
    private val _basic = MutableStateFlow("")
    private val _reachable = MutableStateFlow(true)
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()
    val basic: StateFlow<String> = _basic.asStateFlow()

    fun updateUrl(newUrl: String) {
        _baseUrl.value = newUrl
    }

    fun updateBasic(newBasic: String) {
        _basic.value = newBasic
    }

    fun setReachable(reachable: Boolean) {
        _reachable.value = reachable
    }
}

class DynamicUrlInterceptor @Inject constructor(
    private val urlManager: UrlManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url().toString()

        val newUrl = originalUrl.replace(
            originalRequest.url().host(),
            urlManager.baseUrl.value
                .replace("http://", "")
        )

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .header("Authorization", "Basic " + urlManager.basic.value)
            .build()

        return try {
            return chain.proceed(newRequest)
        } catch (e: Exception) {
            urlManager.setReachable(false)
            throw e
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    fun provideOkHttpClient(
        interceptor: DynamicUrlInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://placeholder")
        .build()

    @Provides
    fun provideApiService(
        retrofit: Retrofit
    ): ImpressionBackendService = retrofit.create(ImpressionBackendService::class.java)
}