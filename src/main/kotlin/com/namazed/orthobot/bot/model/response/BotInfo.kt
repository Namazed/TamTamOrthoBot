package com.namazed.orthobot.bot.model
import com.google.gson.annotations.SerializedName


class BotInfo(
    @SerializedName("name") val name: String,
    @SerializedName("user_id") val userId: Long
)