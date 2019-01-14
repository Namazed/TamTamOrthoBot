package com.namazed.orthobot.bot.model

import com.google.gson.annotations.SerializedName


class Updates(
    @SerializedName("updates") val listUpdates: List<Update>
)

class Update(
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("update_type") val updateType: String,
    @SerializedName("message_id") val deletedOrRestoredMessageId: String = "",
    @SerializedName("message") val message: Message = EMPTY_MESSAGE,
    @SerializedName("callback") val callback: Callback = EMPTY_CALLBACK
)

//sealed class UpdateTypes {
//    class MessageCreated(type: String, val message: Message) : UpdateTypes()
//}