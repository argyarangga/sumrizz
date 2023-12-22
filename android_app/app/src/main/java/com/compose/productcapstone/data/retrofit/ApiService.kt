package com.compose.productcapstone.data.retrofit

import com.compose.productcapstone.data.response.SummarizeResponse
import com.compose.productcapstone.data.response.SummarizeResult
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("transcript")
    suspend fun result(
        @Body request: SummarizeResponse
    ): Response<SummarizeResult>
}

