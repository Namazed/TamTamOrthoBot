package com.namazed.orthobot.bot.model.request

import com.google.gson.annotations.SerializedName
import com.namazed.orthobot.bot.model.request.AttachType.*
import java.lang.IllegalArgumentException

class AttachmentKeyboard(
    @SerializedName("type") val type: String = "",
    @SerializedName("payload") val payload: InlineKeyboard = EMPTY_INLINE_KEYBOARD
)

fun attachTypeFrom(value: String) = when(value.toUpperCase()) {
    "IMAGE" -> IMAGE
    "VIDEO" -> VIDEO
    "AUDIO" -> AUDIO
    "FILE" -> FILE
    "CONTACT" -> CONTACT
    "STICKER" -> STICKER
    "INLINE_KEYBOARD" -> INLINE_KEYBOARD
    else -> throw IllegalArgumentException("Unknown type")
}

enum class AttachType(val value: String) {
    IMAGE("IMAGE"),
    VIDEO("VIDEO"),
    AUDIO("AUDIO"),
    FILE("FILE"),
    CONTACT("CONTACT"),
    STICKER("STICKER"),
    INLINE_KEYBOARD("INLINE_KEYBOARD")
}