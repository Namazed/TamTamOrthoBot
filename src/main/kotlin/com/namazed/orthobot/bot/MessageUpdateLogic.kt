package com.namazed.orthobot.bot

import chat.tamtam.botsdk.client.ResultRequest
import chat.tamtam.botsdk.model.CallbackId
import chat.tamtam.botsdk.model.ChatId
import chat.tamtam.botsdk.model.UserId
import chat.tamtam.botsdk.model.response.Callback
import chat.tamtam.botsdk.model.response.Default
import chat.tamtam.botsdk.model.response.SendMessage
import chat.tamtam.botsdk.state.MessageState
import com.namazed.orthobot.client.HttpClientManager
import com.namazed.orthobot.client.model.createDictionaryText
import com.namazed.orthobot.db.UpdateStateService
import org.slf4j.Logger

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
        sendFunction: suspend (String) -> ResultRequest<SendMessage>,
        clear: suspend (CallbackId) -> ResultRequest<Default>
    ) {
        typingOn(ChatId(state.message.recipient.chatId))
        val translateResult = clientManager.translate(state.message.messageInfo.text, lang)
        log.info("translate lang = $lang, ${translateResult.lang}")

        val userId = UserId(state.message.sender.userId)

        val callbackId = CallbackId(callback.callbackId)
        updateStateService.updateState(
            userId,
            TranslateState.Result(userId, callbackId, state.message, translateResult)
        )
        sendFunction(translateResult.text[0])
        clear(CallbackId(callback.callbackId))
        typingOff(ChatId(state.message.recipient.chatId))
    }

    suspend fun processInputWord(
        state: MessageState,
        callback: Callback,
        typingOn: suspend (ChatId) -> Unit,
        typingOff: suspend (ChatId) -> Unit,
        sendFunction: suspend (String, UserId) -> ResultRequest<SendMessage>,
        clear: suspend (CallbackId) -> ResultRequest<Default>
    ) {
        typingOn(ChatId(state.message.recipient.chatId))
        log.info("processInputWord")
        val split = state.message.messageInfo.text.split(Regex(" "))
        val dictionary = createDictionaryText(clientManager.loadDictionary(split[0]))
        log.info("processInputWord: Loaded dictionary")

        val callbackId = CallbackId(callback.callbackId)
        val userId = UserId(state.message.sender.userId)

        updateStateService.updateState(userId, DictionaryState.Result(userId, callbackId, state.message, dictionary))
        sendFunction(dictionary, userId)

        clear(callbackId)
        typingOff(ChatId(state.message.recipient.chatId))
    }

}