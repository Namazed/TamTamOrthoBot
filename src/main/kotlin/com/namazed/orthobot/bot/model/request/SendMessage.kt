package com.namazed.orthobot.bot.model.request

import com.google.gson.annotations.SerializedName
import com.namazed.orthobot.bot.Payloads
import com.namazed.orthobot.bot.UpdateState

class SendMessage(
    @SerializedName("text") val text: String = "",
    @SerializedName("attachments") val attachments: List<AttachmentKeyboard> = emptyList(),
    @SerializedName("notify") val notifyUser: Boolean = true
)


fun createMessage(state: UpdateState) = when (state) {
    is UpdateState.StartState -> SendMessage(initialText(state.message.sender.name), createAttachmentKeyboard())
    else -> SendMessage()
}

fun initialText(name: String): String {
    return """Приветствую тебя, $name.
        |Похоже у тебя серьезные проблемы, раз ты обратился ко мне.
        |Учти я могу помочь тебе только в двух случаях.
        |Первый это проверить твой текст на орфографические ошибки.
        |Второй это предоставить тебе значение определенного слова.
        |И не пытайся оживлять мертвых с помощью моей силы, сгоришь в аду за это!
    """.trimMargin()
}

fun createAttachmentKeyboard() = listOf(AttachmentKeyboard(AttachType.INLINE_KEYBOARD.value.toLowerCase(), createInlineKeyboard()))

private fun createInlineKeyboard() = InlineKeyboard(
    listOf(
        listOf(
            Button(ButtonType.CALLBACK.value.toLowerCase(), "Значение слова", payload = Payloads.DICTIONARY),
            Button(ButtonType.CALLBACK.value.toLowerCase(), "Проверка орфографии", payload = Payloads.ORTHO)
        )
    )
)
