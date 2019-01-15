package com.namazed.orthobot.bot.model.request

import com.google.gson.annotations.SerializedName
import com.namazed.orthobot.bot.Payloads
import com.namazed.orthobot.bot.UpdateState
import com.namazed.orthobot.client.model.Dictionary
import com.namazed.orthobot.client.model.Main
import org.slf4j.Logger

class SendMessage(
    @SerializedName("text") val text: String = "",
    @SerializedName("attachments") val attachments: List<AttachmentKeyboard> = emptyList(),
    @SerializedName("notify") val notifyUser: Boolean = true
)

fun createMessage(state: UpdateState?, log: Logger) = when (state) {
    is UpdateState.StartState -> {
        log.info("createMessage: Start state")
        SendMessage(initialText(state.message.sender.name), createAttachmentKeyboard(state))
    }
    is UpdateState.BackState -> {
        log.info("createMessage: Back state")
        SendMessage(standardText(state.callback.user.name), createAttachmentKeyboard(state))
    }
    is UpdateState.OrthoState.InputText -> {
        log.info("createMessage: Ortho input text state")
        SendMessage(inputText(), createAttachmentKeyboard(state))
    }
    is UpdateState.DictionaryState.InputWord -> {
        log.info("createMessage: Dictionary input word state")
        SendMessage(inputWordText(), createAttachmentKeyboard(state))
    }
    is UpdateState.DictionaryState.Result -> {
        log.info("createMessage: Dictionary result state")
        SendMessage(createDictionaryText(state.dictionary), createAttachmentKeyboard(state))
    }
    else -> SendMessage()
}

fun createDictionaryText(dictionary: Dictionary): String {
    val stringBuilder = StringBuilder()
    dictionary.def[0].main.asIterable().mapIndexed { index, mainInfo: Main ->
        if (index == 0) {
            stringBuilder.append("Значение: [ ${mainInfo.text} ]")
        } else {
            stringBuilder.append("\nЗначение: [ ${mainInfo.text} ]")
        }
        if (mainInfo.synonyms.isNotEmpty()) {
            stringBuilder.append("\nСинонимы:\n")
            stringBuilder.append("[ ")
        }
        mainInfo.synonyms.asIterable().mapIndexed { synIndex, synonym ->
            stringBuilder.append(synonym.text)
            if (synIndex < mainInfo.synonyms.size - 1) {
                stringBuilder.append(", ")
            }
            synonym
        }
        if (mainInfo.synonyms.isNotEmpty()) {
            stringBuilder.append(" ]\n")
        }
        mainInfo
    }

    return stringBuilder.toString()
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

fun standardText(name: String): String {
    return """Выбор за тобой, $name.
    """.trimMargin()
}

fun createAttachmentKeyboard(state: UpdateState) = listOf(
    AttachmentKeyboard(
        AttachType.INLINE_KEYBOARD.value.toLowerCase(),
        when (state) {
            is UpdateState.OrthoState.InputText -> createInlineKeyboardWithBackButton()
            is UpdateState.DictionaryState.InputWord -> createInlineKeyboardWithBackButton()
            else -> createStandardInlineKeyboard()
        }
    )
)

private fun createStandardInlineKeyboard() = InlineKeyboard(
    listOf(
        listOf(
            Button(ButtonType.CALLBACK.value.toLowerCase(), "Значение слова", payload = Payloads.DICTIONARY_INPUT),
            Button(ButtonType.CALLBACK.value.toLowerCase(), "Проверка орфографии", payload = Payloads.ORTHO_INPUT)
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
