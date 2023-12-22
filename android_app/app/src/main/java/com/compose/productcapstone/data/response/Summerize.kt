package com.compose.productcapstone.data.response

import com.google.gson.annotations.SerializedName

data class SummarizeResult(
    @SerializedName("status")
    val error: String,
    @SerializedName("body")
    val body: String
)

data class SummarizeResponse(
    @SerializedName("type")
    val type: String,
    @SerializedName("language")
    val language: String,
    @SerializedName("source")
    val source: String
)
