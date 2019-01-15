package com.namazed.orthobot.bot.model.response

import com.google.gson.annotations.SerializedName

class AnswerCallback(
    @SerializedName("success") val success: Boolean
)