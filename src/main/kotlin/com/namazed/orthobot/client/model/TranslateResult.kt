package com.namazed.orthobot.client.model
import com.google.gson.annotations.SerializedName


class TranslateResult(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("lang") val lang: String = "",
    @SerializedName("text") val text: List<String> = emptyList()
)