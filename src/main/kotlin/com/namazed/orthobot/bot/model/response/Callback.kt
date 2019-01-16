package com.namazed.orthobot.bot.model.response

import com.google.gson.annotations.SerializedName

val EMPTY_CALLBACK = Callback()

class Callback(
    @SerializedName("timestamp") val timestamp: Long = -1,
    @SerializedName("callback_id") val callbackId: String = "",
    @SerializedName("payload") val payload: String = "",
    @SerializedName("user") val user: User = User()
)

class User(
    @SerializedName("user_id") val userId: Long = -1,
    @SerializedName("name") val name: String = ""
)

fun isNotEmptyCallback(callback: Callback?) =
    callback != null && callback.timestamp != -1L