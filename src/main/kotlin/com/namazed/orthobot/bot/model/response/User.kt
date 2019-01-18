package com.namazed.orthobot.bot.model.response

import com.google.gson.annotations.SerializedName

class User(
    @SerializedName("user_id") val userId: Long = -1,
    @SerializedName("name") val name: String = ""
)