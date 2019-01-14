package com.namazed.orthobot.bot.model.request

import com.google.gson.annotations.SerializedName
import com.namazed.orthobot.bot.model.request.ButtonType.*
import java.lang.IllegalArgumentException

val EMPTY_INLINE_KEYBOARD = InlineKeyboard()

class InlineKeyboard(
    @SerializedName("buttons") val buttons: List<List<Button>> = emptyList()
)

class Button(
    @SerializedName("type") val type: String,
    @SerializedName("text") val title: String,
    @SerializedName("intent") val intent: String = ButtonIntent.DEFAULT.value.toLowerCase(),
    @SerializedName("payload") val payload: String = ""
)


fun buttonTypeFrom(value: String) = when(value.toUpperCase()) {
    "CALLBACK" -> CALLBACK
    "LINK" -> LINK
    "REQUEST_CONTACT" -> REQUEST_CONTACT
    "REQUEST_GEO_LOCATION" -> REQUEST_GEO_LOCATION
    else -> throw IllegalArgumentException("Unknown value")
}

enum class ButtonType(val value: String) {
    CALLBACK("CALLBACK"),
    LINK("LINK"),
    REQUEST_CONTACT("REQUEST_CONTACT"),
    REQUEST_GEO_LOCATION("REQUEST_GEO_LOCATION")
}

enum class ButtonIntent(val value: String) {
    DEFAULT("DEFAULT"),
    POSITIVE("POSITIVE"),
    NEGATIVE("NEGATIVE")
}