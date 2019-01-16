package com.namazed.orthobot.bot.model.request

import com.google.gson.annotations.SerializedName
import com.namazed.orthobot.bot.*
import org.slf4j.Logger

class SendMessage(
    @SerializedName("text") val text: String? = null,
    @SerializedName("attachments") val attachments: List<AttachmentKeyboard> = emptyList(),
    @SerializedName("notify") val notifyUser: Boolean = true
)

fun createMessage(state: UpdateState?, log: Logger) = when (state) {
    is StartState -> {
        log.info("createMessage: Start state")
        SendMessage(initialText(state.message.sender.name), createListAttachmentKeyboard(state))
    }
    is BackState -> {
        log.info("createMessage: Back state")
        SendMessage(standardText(state.callback.user.name), createListAttachmentKeyboard(state))
    }
    is OrthoState.InputText -> {
        log.info("createMessage: Ortho input text state")
        SendMessage(inputText(), createListAttachmentKeyboard(state))
    }
    is DictionaryState.InputWord -> {
        log.info("createMessage: Dictionary input word state")
        SendMessage(inputWordText(), createListAttachmentKeyboard(state))
    }
    is DictionaryState.Result -> {
        log.info("createMessage: Dictionary result state")
        SendMessage(state.dictionary, createListAttachmentKeyboard(state))
    }
    is TranslateState.TranslateEn -> {
        log.info("createMessage: Translate on english state")
        SendMessage(inputTranslateText("английский"), createListAttachmentKeyboard(state))
    }
    is TranslateState.TranslateRu -> {
        log.info("createMessage: Translate on russian state")
        SendMessage(inputTranslateText("русский"), createListAttachmentKeyboard(state))
    }
    is TranslateState.Result -> {
        log.info("createMessage: Translate result state")
        SendMessage(state.translateResult.text[0], createListAttachmentKeyboard(state))
    }
    is ClearState -> {
        log.info("createMessage: Clear state")
        SendMessage(state.messageText)
    }
    else -> SendMessage()
}

fun initialText(name: String): String {
    return """Приветствую тебя, $name.
        |Похоже у тебя серьезные проблемы, раз ты обратился ко мне.
        |МОЯ МОЩЬ БЕЗМЕРНА!
        |Но ты пока потыкай эти кнопки, потренируйся.
        |И не пытайся оживлять мертвых с помощью моей силы, сгоришь в аду за это!
    """.trimMargin()
}

fun resultText(name: String): String {
    return """Спасибо, $name, что воспользовались моими услугами, с вас 0 руб.
        |Хотите повторить?
    """.trimMargin()
}

fun inputText(): String {
    return """Введите, пожалуйста, текст который хотите проверить.
    """.trimMargin()
}

fun inputWordText(): String {
    return """Введите, пожалуйста, слово для которого вы хотите получит значение.
    """.trimMargin()
}

fun inputTranslateText(lang: String): String {
    return """Введите, пожалуйста, текст который хотите перевести на $lang.
    """.trimMargin()
}

fun standardText(name: String): String {
    return """Выбор за тобой, $name.
    """.trimMargin()
}

fun createListAttachmentKeyboard(state: UpdateState): List<AttachmentKeyboard> {
    if (state is TranslateState.Result || state is DictionaryState.Result || state is OrthoState.Result) {
        return emptyList()
    }

    return listOf(createAttachmentKeyboard(state))
}

private fun createAttachmentKeyboard(state: UpdateState): AttachmentKeyboard {
    return AttachmentKeyboard(
        AttachType.INLINE_KEYBOARD.value.toLowerCase(),
        when (state) {
            is OrthoState.InputText, is DictionaryState.InputWord,
            is TranslateState.TranslateEn, is TranslateState.TranslateRu ->
                createInlineKeyboardWithBackButton()
            else -> createStandardInlineKeyboard()
        }
    )
}

private fun createStandardInlineKeyboard() = InlineKeyboard(
    listOf(
        listOf(
            Button(ButtonType.CALLBACK.value.toLowerCase(), "Значение слова", payload = Payloads.DICTIONARY_INPUT),
            Button(ButtonType.CALLBACK.value.toLowerCase(), "Проверка орфографии", payload = Payloads.ORTHO_INPUT)
        ),
        listOf(
            Button(ButtonType.CALLBACK.value.toLowerCase(), "Перевести текст на английский", payload = Payloads.TRANSLATE_EN),
            Button(ButtonType.CALLBACK.value.toLowerCase(), "Перевести текст на русский", payload = Payloads.TRANSLATE_RU)
        )
    )
)

private fun createInlineKeyboardWithBackButton() = InlineKeyboard(
    listOf(
        listOf(
            Button(ButtonType.CALLBACK.value.toLowerCase(), "Вернуться", payload = Payloads.BACK)
        )
    )
)
