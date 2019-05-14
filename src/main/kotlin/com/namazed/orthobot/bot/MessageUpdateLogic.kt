package com.namazed.orthobot.bot

import chat.tamtam.botsdk.client.ResultRequest
import chat.tamtam.botsdk.model.CallbackId
import chat.tamtam.botsdk.model.ChatId
import chat.tamtam.botsdk.model.prepared.Callback
import chat.tamtam.botsdk.model.prepared.Message
import chat.tamtam.botsdk.model.response.Default
import chat.tamtam.botsdk.state.MessageState
import com.namazed.orthobot.client.HttpClientManager
import com.namazed.orthobot.client.model.createDictionaryText
import com.namazed.orthobot.db.UpdateStateService
import org.slf4j.Logger

val YANDEX_TEXT = """
    |
    |Переведено сервисом «Яндекс.Переводчик»
    |http://translate.yandex.ru/
""".trimMargin()

class MessageUpdateLogic(
    private val clientManager: HttpClientManager,
    private val updateStateService: UpdateStateService,
    private val log: Logger
) {

    suspend fun translate(
        lang: String,
        state: MessageState,
        callback: Callback,
        typingOn: suspend (ChatId) -> Unit,
        typingOff: suspend (ChatId) -> Unit,
        sendFunction: suspend (String) -> ResultRequest<Message>,
        clear: suspend (CallbackId) -> ResultRequest<Default>
    ) {
        typingOn(state.message.recipient.chatId)
        val translateResult = clientManager.translate(state.message.body.text, lang)
        log.info("translate lang = $lang, ${translateResult.lang}")

        val userId = state.message.sender.userId

        val callbackId = callback.callbackId
        updateStateService.updateState(
            userId,
            TranslateState.Result(userId, callbackId, state.message, translateResult)
        )
        sendFunction("${translateResult.text[0]}\n$YANDEX_TEXT")
        clear(callback.callbackId)
        typingOff(state.message.recipient.chatId)
    }

    suspend fun processInputWord(
        state: MessageState,
        callback: Callback,
        typingOn: suspend (ChatId) -> Unit,
        typingOff: suspend (ChatId) -> Unit,
        sendFunction: suspend (String) -> ResultRequest<Message>,
        clear: suspend (CallbackId) -> ResultRequest<Default>
    ) {
        typingOn(state.message.recipient.chatId)
        log.info("processInputWord")
        val split = state.message.body.text.split(Regex(" "))
        val dictionary = createDictionaryText(clientManager.loadDictionary(split[0]))
        log.info("processInputWord: Loaded dictionary")

        val callbackId = callback.callbackId
        val userId = state.message.sender.userId

        updateStateService.updateState(userId, DictionaryState.Result(userId, callbackId, state.message, dictionary))
        sendFunction(dictionary)

        clear(callbackId)
        typingOff(state.message.recipient.chatId)
    }

}