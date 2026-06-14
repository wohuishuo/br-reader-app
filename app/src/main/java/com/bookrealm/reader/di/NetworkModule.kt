package com.bookrealm.reader.di

import com.bookrealm.reader.data.remote.ApiConfig
import com.bookrealm.reader.data.remote.LibraryApi
import com.bookrealm.reader.data.remote.UserCenterApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

/** 区分两套后端的 Retrofit 实例(用法:@UserCenterRetrofit retrofit: Retrofit)。 */
@Qualifier annotation class UserCenterRetrofit
@Qualifier annotation class LibraryRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json { ignoreUnknownKeys = true }

    @Provides @Singleton
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        // R3 在这里加:从 DataStore 取 JWT 写入 Authorization 头的拦截器
        .build()

    @Provides @Singleton @UserCenterRetrofit
    fun userCenterRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.USER_CENTER_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton @LibraryRetrofit
    fun libraryRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.LIBRARY_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton
    fun userCenterApi(@UserCenterRetrofit retrofit: Retrofit): UserCenterApi =
        retrofit.create(UserCenterApi::class.java)

    @Provides @Singleton
    fun libraryApi(@LibraryRetrofit retrofit: Retrofit): LibraryApi =
        retrofit.create(LibraryApi::class.java)
}
