package com.namazed.orthobot.bot

import com.namazed.orthobot.bot.model.CallbackId
import com.namazed.orthobot.bot.model.UserId
import com.namazed.orthobot.bot.model.request.AnswerCallback as RequestAnswerCallback
import com.namazed.orthobot.bot.model.response.AnswerCallback as ResponseAnswerCallback
import com.namazed.orthobot.bot.model.request.createMessage
import com.namazed.orthobot.bot.model.response.*
import com.namazed.orthobot.client.HttpClientManager
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import com.namazed.orthobot.bot.model.response.SendMessage as ResponseSendMessage
import com.namazed.orthobot.bot.model.request.SendMessage as RequestSendMessage

private val PROFILE_TAG_PATTERN = Regex("@([A-Za-z0-9_-]+)")
private val START_COMMAND_PATTERN = Regex("(?i)/start")
private val ORTHO_COMMAND_PATTERN = Regex("(?i)/ortho")
private val DICTIONARY_COMMAND_PATTERN = Regex("(?i)/dictionary")
private val BOTS_COMMAND_PATTERN = Regex("/[\\p{L}\\p{N}_]+($PROFILE_TAG_PATTERN)?")

fun isStartCommand(text: String) = START_COMMAND_PATTERN.matches(text)
fun isOrthoCommand(text: String) = ORTHO_COMMAND_PATTERN.matches(text)
fun isDictionaryCommand(text: String) = DICTIONARY_COMMAND_PATTERN.matches(text)

fun isUnknownCommand(text: String) = BOTS_COMMAND_PATTERN.matches(text)

var userUpdateState: UpdateState = UpdateState.StartState(UserId(-1), Message())

class CommandParser(val clientManager: HttpClientManager, val log: Logger) {

    suspend inline fun processUpdates(
        updates: Updates,
        sendMessageRequest: (UserId, RequestSendMessage) -> ResponseSendMessage,
        answerRequest: (CallbackId, RequestAnswerCallback) -> ResponseAnswerCallback
    ) {
        updates.listUpdates.forEach {
            when {
                isNotEmptyMessage(it.message) && isStartCommand(it.message.messageInfo.text) ->
                    handleStartCommand(it, sendMessageRequest)

                isNotEmptyCallback(it.callback) && it.callback.payload == Payloads.ORTHO_INPUT ->
                    handleStartOrtho(it, answerRequest)

                isNotEmptyMessage(it.message) && userUpdateState is UpdateState.OrthoState.InputText ->
                    handleInputText(it, sendMessageRequest)

                isNotEmptyCallback(it.callback) && it.callback.payload == Payloads.DICTIONARY_INPUT ->
                    handleStartDictionary(it, answerRequest)

                isNotEmptyMessage(it.message) && userUpdateState is UpdateState.DictionaryState.InputWord ->
                    handleInputWord(it, sendMessageRequest)

                isNotEmptyCallback(it.callback) && (it.callback.payload == Payloads.TRANSLATE_EN || it.callback.payload == Payloads.TRANSLATE_RU) ->
                    handleTranslate(it, answerRequest)

                isNotEmptyMessage(it.message) && userUpdateState is UpdateState.TranslateState ->
                    handleTranslateResult(it, sendMessageRequest)

                isNotEmptyCallback(it.callback) && it.callback.payload == Payloads.BACK ->
                    handleBack(it, answerRequest)
            }
        }
    }

    suspend inline fun handleTranslateResult(update: Update, sendMessageRequest: (UserId, RequestSendMessage) -> ResponseSendMessage) {
        log.info("processUpdates -> handleTranslateResult")
        val translateResult = withContext(clientManager.clientDispatcher) {
            val lang = if (userUpdateState is UpdateState.TranslateState.TranslateRu) "ru" else "en"
            clientManager.translate(update.message.messageInfo.text, lang)
        }
        log.info("processUpdates -> handleTranslateResult, ${translateResult.lang}")
        val callbackId = if (userUpdateState is UpdateState.TranslateState.TranslateRu) {
            CallbackId((userUpdateState as UpdateState.TranslateState.TranslateRu).callback.callbackId)
        } else {
            CallbackId((userUpdateState as UpdateState.TranslateState.TranslateEn).callback.callbackId)
        }
        val userId = UserId(update.message.sender.userId)
        userUpdateState = UpdateState.TranslateState.Result(userId, callbackId, update.message, translateResult)
        sendMessageRequest(userId, createMessage(userUpdateState, log))
    }

    inline fun handleTranslate(update: Update, answerRequest: (CallbackId, RequestAnswerCallback) -> ResponseAnswerCallback) {
        log.info("processUpdates -> handleTranslate, payloads = ${update.callback.payload}")
        userUpdateState = if (update.callback.payload == Payloads.TRANSLATE_EN) {
            UpdateState.TranslateState.TranslateEn(UserId(update.callback.user.userId), update.callback)
        } else {
            UpdateState.TranslateState.TranslateRu(UserId(update.callback.user.userId), update.callback)
        }
        answerRequest(
            CallbackId(update.callback.callbackId),
            RequestAnswerCallback(update.callback.user.userId, createMessage(userUpdateState, log))
        )
    }

    inline fun handleStartCommand(update: Update, sendMessageRequest: (UserId, RequestSendMessage) -> ResponseSendMessage) {
        log.info("processUpdates -> handleStartCommand")
        userUpdateState = UpdateState.StartState(UserId(update.message.sender.userId), update.message)
        sendMessageRequest(UserId(update.message.sender.userId), createMessage(userUpdateState, log))
    }

    inline fun handleStartOrtho(update: Update, answerRequest: (CallbackId, RequestAnswerCallback) -> ResponseAnswerCallback) {
        log.info("processUpdates -> handleStartOrtho")
        userUpdateState = UpdateState.OrthoState.InputText(UserId(update.callback.user.userId), update.callback)
        answerRequest(
            CallbackId(update.callback.callbackId),
            RequestAnswerCallback(update.callback.user.userId, createMessage(userUpdateState, log))
        )
    }

    inline fun handleStartDictionary(update: Update, answerRequest: (CallbackId, RequestAnswerCallback) -> ResponseAnswerCallback) {
        log.info("processUpdates -> handleStartDictionary")
        userUpdateState = UpdateState.DictionaryState.InputWord(UserId(update.callback.user.userId), update.callback)
        answerRequest(
            CallbackId(update.callback.callbackId),
            RequestAnswerCallback(update.callback.user.userId, createMessage(userUpdateState, log))
        )
    }

    suspend inline fun handleInputText(update: Update, sendMessageRequest: (UserId, RequestSendMessage) -> ResponseSendMessage) {
        log.info("processUpdates -> handleInputText")
        val checkResult = withContext(clientManager.clientDispatcher) {
            clientManager.spellCheck(update.message.messageInfo.text)
        }
        val callbackId = CallbackId((userUpdateState as UpdateState.OrthoState.InputText).callback.callbackId)
        val userId = UserId(update.message.sender.userId)
        userUpdateState = UpdateState.OrthoState.Result(userId, callbackId, update.message)
        sendMessageRequest(userId, createMessage(userUpdateState, log))
    }

    inline fun handleBack(update: Update, answerRequest: (CallbackId, RequestAnswerCallback) -> ResponseAnswerCallback) {
        log.info("processUpdates -> handleBack")
        userUpdateState = UpdateState.BackState(UserId(update.callback.user.userId), update.callback)
        answerRequest(CallbackId(update.callback.callbackId), RequestAnswerCallback(update.callback.user.userId, createMessage(userUpdateState, log)))
    }

    suspend inline fun handleInputWord(update: Update, sendMessageRequest: (UserId, RequestSendMessage) -> ResponseSendMessage) {
        log.info("processUpdates -> handleInputWord")
        val dictionary = withContext(clientManager.clientDispatcher) {
            val split = update.message.messageInfo.text.split(Regex(" "))
            clientManager.loadDictionary(split[0])
        }
        log.info("processUpdates -> handleInputWord: Loaded dictionary")
        val callbackId = CallbackId((userUpdateState as UpdateState.DictionaryState.InputWord).callback.callbackId)
        val userId = UserId(update.message.sender.userId)
        userUpdateState = UpdateState.DictionaryState.Result(userId, callbackId, update.message, dictionary)
        sendMessageRequest(userId, createMessage(userUpdateState, log))
    }
}