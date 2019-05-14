package com.namazed.orthobot.bot.model

import chat.tamtam.botsdk.keyboard.keyboard
import chat.tamtam.botsdk.model.Button
import chat.tamtam.botsdk.model.ButtonType
import com.namazed.orthobot.bot.Payloads


fun initialText(name: String): String {
    return """Приветствую тебя, $name.
        |Похоже у тебя серьезные проблемы, раз ты обратился ко мне.
        |Чем я могу помочь?
    """.trimMargin()
}

fun inputText(): String {
    return """Введите, пожалуйста, текст который хотите проверить.
    """.trimMargin()
}

fun inputWordText(): String {
    return """Введите, пожалуйста, слово для которого вы хотите получить значение.
    """.trimMargin()
}

fun inputDoesntWorkText(): String {
    return """Извините, но в данный момент этот функционал находится на профилактике.
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

fun createAllActionsInlineKeyboard() = keyboard {
    +buttonRow {
        +Button(ButtonType.CALLBACK, "Значение слова", payload = Payloads.DICTIONARY_INPUT)
        +Button(ButtonType.CALLBACK, "Проверка орфографии", payload = Payloads.ORTHO_INPUT)
    }
    +buttonRow {
        +Button(ButtonType.CALLBACK, "Перевести на En", payload = Payloads.TRANSLATE_EN)
        +Button(ButtonType.CALLBACK, "Перевести на Ru", payload = Payloads.TRANSLATE_RU)

    }
}

fun createBackButtonInlineKeyboard() = keyboard {
    +buttonRow {
        +Button(ButtonType.CALLBACK, "Вернуться", payload = Payloads.BACK)
    }
}
