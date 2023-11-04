package com.practice.testing_practice.data.remote

import com.practice.testing_practice.BuildConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayApi {
    @GET("/api/")
    suspend fun searchForImage(
        @Query("q") searchQuery: String,
        @Query("key") apkKey: String = BuildConfig.API_KEY
    ): Response<ImageResponse>
}