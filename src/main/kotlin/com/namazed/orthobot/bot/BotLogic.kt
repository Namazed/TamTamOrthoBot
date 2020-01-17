package com.namazed.orthobot.bot

import chat.tamtam.botsdk.Coordinator
import chat.tamtam.botsdk.client.ResultRequest
import chat.tamtam.botsdk.communications.LongPollingStartingParams
import chat.tamtam.botsdk.communications.WebhookStartingParams
import chat.tamtam.botsdk.communications.longPolling
import chat.tamtam.botsdk.communications.webhook
import chat.tamtam.botsdk.model.CallbackId
import chat.tamtam.botsdk.model.ChatId
import chat.tamtam.botsdk.model.prepared.Callback
import chat.tamtam.botsdk.model.prepared.Message
import chat.tamtam.botsdk.model.request.AnswerParams
import chat.tamtam.botsdk.model.request.Subscription
import chat.tamtam.botsdk.model.response.Default
import chat.tamtam.botsdk.scopes.BotScope
import chat.tamtam.botsdk.scopes.CallbacksScope
import chat.tamtam.botsdk.scopes.MessagesScope
import com.namazed.orthobot.ApiKeys
import com.namazed.orthobot.bot.model.*
import com.namazed.orthobot.db.UpdateStateService
import org.slf4j.Logger

class BotLogic(
    private val updateStateService: UpdateStateService,
    private val apiKeys: ApiKeys,
    private val messageUpdateLogic: MessageUpdateLogic,
    private val log: Logger
) {

    private var coordinator: Coordinator? = null

    fun parseUpdates(updatesJson: String) {
        coordinator?.coordinateAsync(updatesJson)
    }

    fun start(webhook: Boolean) {
        coordinator = if (webhook) {
            webhook(WebhookStartingParams(apiKeys.botApi,
                subscription = Subscription(url = "https://secure-scrubland-29690.herokuapp.com/updates"))) {
                updatesProcessing()
            }
        } else {
            longPolling(LongPollingStartingParams(apiKeys.botApi)) { updatesProcessing() }
        }
    }

    private fun BotScope.updatesProcessing() {
        onStartBot {
            handleCommandWithAllActions(it, updateStateService, initialText(it.user.name))
        }

        commands {

            onCommand("/start") {
                log.info("onCommand /start")
                handleCommandWithAllActions(it, updateStateService, initialText(it.command.message.sender.name))
            }

            onCommand("/actions") {
                log.info("onCommand /actions")
                handleCommandWithAllActions(it, updateStateService, standardText(it.command.message.sender.name))
            }

            onUnknownCommand {
                typingOn(it.command.message.recipient.chatId)
                """Неизвестная команда, я работаю только с:
                        |/start
                        |/actions""".trimMargin() sendFor it.command.message.recipient.chatId
                typingOff(it.command.message.recipient.chatId)
            }
        }

        callbacks {
            answerOnCallbacks()
        }

        messages {
            answerOnMessages()
        }
    }

    private fun CallbacksScope.answerOnCallbacks() {
        answerOnCallback(Payloads.BACK) {
            updateStateService.updateState(
                it.callback.user.userId,
                BackState(it.callback.user.userId, it.callback)
            )
            standardText(it.callback.user.name) prepareReplacementCurrentMessage AnswerParams(
                it.callback.callbackId
            ) answerWith createAllActionsInlineKeyboard()
        }

        answerOnCallback(Payloads.DICTIONARY_INPUT) {
            updateStateService.updateState(
                it.callback.user.userId,
                DictionaryState.InputWord(it.callback.user.userId, it.callback)
            )
            inputWordText() prepareReplacementCurrentMessage AnswerParams(
                it.callback.callbackId
            ) answerWith createBackButtonInlineKeyboard()
        }

        answerOnCallback(Payloads.ORTHO_INPUT) {
            updateStateService.updateState(
                it.callback.user.userId,
                OrthoState.InputText(it.callback.user.userId, it.callback)
            )
            inputDoesntWorkText() prepareReplacementCurrentMessage AnswerParams(
                it.callback.callbackId
            ) answerWith createBackButtonInlineKeyboard()
        }

        answerOnCallback(Payloads.TRANSLATE_EN) {
            updateStateService.updateState(
                it.callback.user.userId,
                TranslateState.TranslateEn(it.callback.user.userId, it.callback)
            )
            inputTranslateText("английский") prepareReplacementCurrentMessage AnswerParams(
                it.callback.callbackId
            ) answerWith createBackButtonInlineKeyboard()
        }

        answerOnCallback(Payloads.TRANSLATE_RU) {
            updateStateService.updateState(
                it.callback.user.userId,
                TranslateState.TranslateRu(it.callback.user.userId, it.callback)
            )
            inputTranslateText("русский") prepareReplacementCurrentMessage AnswerParams(
                it.callback.callbackId
            ) answerWith createBackButtonInlineKeyboard()
        }
    }

    private fun MessagesScope.answerOnMessages() {
        answerOnMessage { state ->
            val updateState = updateStateService.selectState(state.message.sender.userId)
            val startTyping: suspend (ChatId) -> Unit = { id -> typingOn(id) }
            val stopTyping: suspend (ChatId) -> Unit = { id -> typingOff(id) }
            val sendFunction: suspend (String) -> ResultRequest<Message> =
                { text -> text sendFor state.message.sender.userId }
            val clearKeyboard: suspend (CallbackId) -> ResultRequest<Default> = { id -> "" answerFor id }

            suspend fun processAction(callback: Callback, translateLang: String? = null) {
                translateLang?.let {
                    messageUpdateLogic.translate(translateLang, state, callback, startTyping, stopTyping,
                        sendFunction, clearKeyboard)
                } ?: messageUpdateLogic.processInputWord(state, callback, startTyping, stopTyping,
                    sendFunction, clearKeyboard)
            }

            when (updateState) {
                is TranslateState.TranslateEn -> processAction(updateState.callback, "en")
                is TranslateState.TranslateRu -> processAction(updateState.callback, "ru")
                is DictionaryState.InputWord -> processAction(updateState.callback)
            }
        }

    }

}