package ru.polyarbeiterz.impressionmap.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.polyarbeiterz.impressionmap.data.service.ImpressionBackendService

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    fun getRetrofit() = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://192.168.0.107:8000")
        .build()

    @Provides
    fun getRetrofitService(retrofit: Retrofit): ImpressionBackendService =
        retrofit.create(ImpressionBackendService::class.java)

}