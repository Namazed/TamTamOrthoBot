package com.namazed.orthobot.bot.model.request

import com.google.gson.annotations.SerializedName

class AnswerCallback(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("message") val message: SendMessage,
    @SerializedName("notification") val notification: String? = null
)