package com.namazed.orthobot.bot

import com.namazed.orthobot.bot.model.CallbackId
import com.namazed.orthobot.bot.model.MessageId
import com.namazed.orthobot.bot.model.UserId
import com.namazed.orthobot.bot.model.request.createMessage
import com.namazed.orthobot.bot.model.response.*
import com.namazed.orthobot.client.HttpClientManager
import com.namazed.orthobot.client.model.createDictionaryText
import com.namazed.orthobot.db.UpdateStateService
import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import com.namazed.orthobot.bot.model.request.AnswerCallback as RequestAnswerCallback
import com.namazed.orthobot.bot.model.request.SendMessage as RequestSendMessage
import com.namazed.orthobot.bot.model.response.AnswerCallback as ResponseAnswerCallback
import com.namazed.orthobot.bot.model.response.SendMessage as ResponseSendMessage

private val PROFILE_TAG_PATTERN = Regex("@([A-Za-z0-9_-]+)")
private val START_COMMAND_PATTERN = Regex("(?i)/start")
private val ACTIONS_COMMAND_PATTERN = Regex("(?i)/actions")
private val ORTHO_COMMAND_PATTERN = Regex("(?i)/ortho")
private val DICTIONARY_COMMAND_PATTERN = Regex("(?i)/dictionary")
private val BOTS_COMMAND_PATTERN = Regex("/[\\p{L}\\p{N}_]+($PROFILE_TAG_PATTERN)?")

fun isStartCommand(text: String) = START_COMMAND_PATTERN.matches(text)
fun isActionsCommand(text: String) = ACTIONS_COMMAND_PATTERN.matches(text)
fun isOrthoCommand(text: String) = ORTHO_COMMAND_PATTERN.matches(text)
fun isDictionaryCommand(text: String) = DICTIONARY_COMMAND_PATTERN.matches(text)

fun isUnknownCommand(text: String) = BOTS_COMMAND_PATTERN.matches(text)

typealias SendMessageRequest = (UserId, RequestSendMessage) -> ResponseSendMessage
typealias AnswerRequest = (CallbackId, RequestAnswerCallback) -> ResponseAnswerCallback
typealias ClearRequest = (MessageId, RequestSendMessage) -> ResponseAnswerCallback

class UpdateParser(val clientManager: HttpClientManager, val updateStateService: UpdateStateService, val log: Logger) {

    val marker: Marker = MarkerFactory.getMarker(UpdateParser::class.java.name)

    suspend inline fun processUpdates(
        updates: Updates,
        sendMessageRequest: SendMessageRequest,
        answerRequest: (CallbackId, RequestAnswerCallback) -> ResponseAnswerCallback,
        clearRequest: (MessageId, RequestSendMessage) -> ResponseAnswerCallback
    ) {
        updates.listUpdates.forEach {
            val userId = getUserId(it)
            val userUpdateState = getUserUpdateState(userId)
            log.info(marker, "processUpdates, userUpdateStateFromDb = ${userUpdateState.updateStateId}")
            when {
                isNotEmptyMessage(it.message) && isStartCommand(it.message.messageInfo.text) ->
                    handleStartCommand(it, sendMessageRequest, userUpdateState)

                isNotEmptyMessage(it.message) && isActionsCommand(it.message.messageInfo.text) ->
                    handleActionsCommand(it, sendMessageRequest, userUpdateState)

                isNotEmptyCallback(it.callback) && it.callback.payload == Payloads.ORTHO_INPUT ->
                    handleStartOrtho(it, answerRequest, userUpdateState)

                isNotEmptyMessage(it.message) && userUpdateState is OrthoState.InputText ->
                    handleInputText(it, sendMessageRequest, clearRequest, userUpdateState)

                isNotEmptyCallback(it.callback) && it.callback.payload == Payloads.DICTIONARY_INPUT ->
                    handleStartDictionary(it, answerRequest, userUpdateState)

                isNotEmptyMessage(it.message) && userUpdateState is DictionaryState.InputWord ->
                    handleInputWord(it, sendMessageRequest, clearRequest, userUpdateState)

                isNotEmptyCallback(it.callback) && (it.callback.payload == Payloads.TRANSLATE_EN || it.callback.payload == Payloads.TRANSLATE_RU) ->
                    handleStartTranslate(it, answerRequest, userUpdateState)

                isNotEmptyMessage(it.message) && userUpdateState is TranslateState ->
                    handleTranslateResult(it, sendMessageRequest, clearRequest, userUpdateState)

                isNotEmptyCallback(it.callback) && it.callback.payload == Payloads.BACK ->
                    handleBack(it, answerRequest, userUpdateState)
            }
        }
    }

    suspend inline fun handleStartCommand(
        update: Update,
        sendMessageRequest: SendMessageRequest,
        userUpdateState: UpdateState
    ) {
        log.info(marker, "processUpdates -> handleStartCommand")
        val userId = UserId(update.message.sender.userId)
        updateStateService.updateState(userUpdateState.updateStateId, StartState(userId, update.message))
        val createdMessage = createMessage(updateStateService.selectState(userId), log)
        val resultMessageInfo = sendMessageRequest(
            userId,
            createdMessage
        )
        updateStateService.updateMessageForEdit(userId, MessageId(resultMessageInfo.messageId), createdMessage.text)
    }

    suspend inline fun handleActionsCommand(
        update: Update,
        sendMessageRequest: SendMessageRequest,
        userUpdateState: UpdateState
    ) {
        log.info(marker, "processUpdates -> handleActionsCommand")
        val userId = UserId(update.message.sender.userId)
        updateStateService.updateState(userUpdateState.updateStateId, StartState(userId, update.message, true))
        val createdMessage = createMessage(updateStateService.selectState(userId), log)
        val resultMessageInfo = sendMessageRequest(
            userId,
            createdMessage
        )
        updateStateService.updateMessageForEdit(userId, MessageId(resultMessageInfo.messageId), createdMessage.text)
    }

    suspend inline fun handleStartOrtho(
        update: Update,
        answerRequest: AnswerRequest,
        userUpdateState: UpdateState
    ) {
        log.info(marker, "processUpdates -> handleStartOrtho")
        updateStateService.updateState(
            userUpdateState.updateStateId,
            OrthoState.InputText(UserId(update.callback.user.userId), update.callback)
        )
        val message = createMessage(updateStateService.selectState(userUpdateState.updateStateId), log)
        answerRequest(
            CallbackId(update.callback.callbackId),
            RequestAnswerCallback(update.callback.user.userId, message)
        )

        updateStateService.updateTextMessageForEdit(userUpdateState.updateStateId, message.text)
    }

    suspend inline fun handleStartDictionary(
        update: Update,
        answerRequest: AnswerRequest,
        userUpdateState: UpdateState
    ) {
        log.info(marker, "processUpdates -> handleStartDictionary")
        updateStateService.updateState(
            userUpdateState.updateStateId,
            DictionaryState.InputWord(UserId(update.callback.user.userId), update.callback)
        )
        val message = createMessage(updateStateService.selectState(userUpdateState.updateStateId), log)
        answerRequest(
            CallbackId(update.callback.callbackId),
            RequestAnswerCallback(update.callback.user.userId, message)
        )

        updateStateService.updateTextMessageForEdit(userUpdateState.updateStateId, message.text)
    }

    suspend inline fun handleInputText(
        update: Update,
        sendMessageRequest: SendMessageRequest,
        clearRequest: ClearRequest,
        userUpdateState: OrthoState.InputText
    ) {
        log.info(marker, "processUpdates -> handleInputText")
        val checkResult = clientManager.spellCheck(update.message.messageInfo.text)
        val callbackId = CallbackId(userUpdateState.callback.callbackId)
        val userId = UserId(update.message.sender.userId)
        updateStateService.updateState(
            userUpdateState.updateStateId,
            OrthoState.Result(userId, callbackId, update.message)
        )
        sendMessageRequest(userId, createMessage(updateStateService.selectState(userId), log))

        val messageForEdit = updateStateService.selectMessageForEdit(userId)
        clearRequest(messageForEdit.messageId, createMessage(ClearState(userId, messageForEdit.text), log))
    }

    suspend inline fun handleInputWord(
        update: Update,
        sendMessageRequest: SendMessageRequest,
        clearRequest: ClearRequest,
        userUpdateState: DictionaryState.InputWord
    ) {
        log.info(marker, "processUpdates -> handleInputWord")
        val split = update.message.messageInfo.text.split(Regex(" "))
        val dictionary = createDictionaryText(clientManager.loadDictionary(split[0]))
        log.info(marker, "processUpdates -> handleInputWord: Loaded dictionary")

        val callbackId = CallbackId(userUpdateState.callback.callbackId)
        val userId = UserId(update.message.sender.userId)

        updateStateService.updateState(userId, DictionaryState.Result(userId, callbackId, update.message, dictionary))
        sendMessageRequest(userId, createMessage(updateStateService.selectState(userId), log))

        val messageForEdit = updateStateService.selectMessageForEdit(userId)
        clearRequest(messageForEdit.messageId, createMessage(ClearState(userId, messageForEdit.text), log))
    }

    suspend inline fun handleStartTranslate(
        update: Update,
        answerRequest: AnswerRequest,
        userUpdateState: UpdateState
    ) {
        log.info(marker, "processUpdates -> handleTranslate, payloads = ${update.callback.payload}")
        val newState = if (update.callback.payload == Payloads.TRANSLATE_EN) {
            TranslateState.TranslateEn(UserId(update.callback.user.userId), update.callback)
        } else {
            TranslateState.TranslateRu(UserId(update.callback.user.userId), update.callback)
        }
        updateStateService.updateState(userUpdateState.updateStateId, newState)
        val state = updateStateService.selectState(userUpdateState.updateStateId)
        val message = createMessage(state, log)
        answerRequest(
            CallbackId(update.callback.callbackId),
            RequestAnswerCallback(update.callback.user.userId, message)
        )
        updateStateService.updateTextMessageForEdit(userUpdateState.updateStateId, message.text)
    }

    suspend inline fun handleTranslateResult(
        update: Update, sendMessageRequest: SendMessageRequest,
        clearRequest: ClearRequest, userUpdateState: TranslateState
    ) {
        log.info(marker, "processUpdates -> handleTranslateResult")
        val lang = if (userUpdateState is TranslateState.TranslateRu) "ru" else "en"
        val translateResult = clientManager.translate(update.message.messageInfo.text, lang)
        log.info(marker, "processUpdates -> handleTranslateResult, ${translateResult.lang}")

        val callbackId = if (userUpdateState is TranslateState.TranslateRu) {
            CallbackId(userUpdateState.callback.callbackId)
        } else {
            CallbackId((userUpdateState as TranslateState.TranslateEn).callback.callbackId)
        }
        val userId = UserId(update.message.sender.userId)

        updateStateService.updateState(
            userUpdateState.updateStateId,
            TranslateState.Result(userId, callbackId, update.message, translateResult)
        )
        sendMessageRequest(userId, createMessage(updateStateService.selectState(userId), log))

        val messageForEdit = updateStateService.selectMessageForEdit(userId)
        clearRequest(messageForEdit.messageId, createMessage(ClearState(userId, messageForEdit.text), log))
    }

    suspend inline fun handleBack(update: Update, answerRequest: AnswerRequest, userUpdateState: UpdateState) {
        log.info(marker, "processUpdates -> handleBack")
        updateStateService.updateState(
            userUpdateState.updateStateId,
            BackState(UserId(update.callback.user.userId), update.callback)
        )
        answerRequest(
            CallbackId(update.callback.callbackId), RequestAnswerCallback(
                update.callback.user.userId,
                createMessage(updateStateService.selectState(userUpdateState.updateStateId), log)
            )
        )
    }

    suspend fun getUserUpdateState(userId: Long?) = userId?.let {
        updateStateService.selectState(UserId(userId))
    } ?: StartState(UserId(-1), Message())

    fun getUserId(update: Update) = when {
        isNotEmptyMessage(update.message) -> update.message.sender.userId
        isNotEmptyCallback(update.callback) -> update.callback.user.userId
        else -> null
    }
}