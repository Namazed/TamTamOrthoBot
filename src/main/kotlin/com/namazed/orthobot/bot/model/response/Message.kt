package com.namazed.orthobot.bot.model.response

import com.google.gson.annotations.SerializedName

val EMPTY_MESSAGE = Message()

class Message(
    @SerializedName("message") val messageInfo: MessageInfo = MessageInfo(),
    @SerializedName("recipient") val recipient: Recipient = Recipient(),
    @SerializedName("sender") val sender: User = User(),
    @SerializedName("timestamp") val timestamp: Long = -1
)

class MessageInfo(
    @SerializedName("mid") val mid: String = "",
    @SerializedName("seq") val seq: Long = -1,
    @SerializedName("text") val text: String = ""
)

class Recipient(
    @SerializedName("chat_id") val chatId: Long = -1
)

fun isNotEmptyMessage(message: Message?) = message != null && message.timestamp != -1L