package com.practice.testing_practice.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayApi {
    @GET("/api/")
    suspend fun searchForImage(
        @Query("q")
    )
}