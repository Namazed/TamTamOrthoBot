package com.namazed.orthobot.bot.model.response

import com.google.gson.annotations.SerializedName

class SendMessage(
    @SerializedName("chat_id") val chatId: Long,
    @SerializedName("message_id") val messageId: String
)