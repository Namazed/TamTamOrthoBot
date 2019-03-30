package com.namazed.orthobot.bot

import chat.tamtam.botsdk.client.ResultRequest
import chat.tamtam.botsdk.communications.longPolling
import chat.tamtam.botsdk.model.CallbackId
import chat.tamtam.botsdk.model.ChatId
import chat.tamtam.botsdk.model.Payload
import chat.tamtam.botsdk.model.UserId
import chat.tamtam.botsdk.model.request.AnswerParams
import chat.tamtam.botsdk.model.response.Default
import chat.tamtam.botsdk.model.response.SendMessage
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

    fun start() {
        longPolling(apiKeys.botApi) {

            onStartBot {
                handleCommandWithAllActions(it, updateStateService, initialText("Человек"))
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
                    typingOn(ChatId(it.command.message.recipient.chatId))
                    """Неизвестная команда, я работаю только с:
                    |/start
                    |/actions""".trimMargin() sendFor ChatId(it.command.message.recipient.chatId)
                    typingOff(ChatId(it.command.message.recipient.chatId))
                }
            }

            callbacks {
                answerOnCallbacks()
            }

            messages {
                answerOnMessages()
            }
        }
    }

    private fun CallbacksScope.answerOnCallbacks() {
        answerOnCallback(Payload(Payloads.BACK)) {
            updateStateService.updateState(
                UserId(it.callback.user.userId),
                BackState(UserId(it.callback.user.userId), it.callback)
            )
            standardText(it.callback.user.name) prepareReplacementCurrentMessage AnswerParams(
                CallbackId(it.callback.callbackId)
            ) answerWith createAllActionsInlineKeyboard()
        }

        answerOnCallback(Payload(Payloads.DICTIONARY_INPUT)) {
            updateStateService.updateState(
                UserId(it.callback.user.userId),
                DictionaryState.InputWord(UserId(it.callback.user.userId), it.callback)
            )
            inputWordText() prepareReplacementCurrentMessage AnswerParams(
                CallbackId(it.callback.callbackId)
            ) answerWith createBackButtonInlineKeyboard()
        }

        answerOnCallback(Payload(Payloads.ORTHO_INPUT)) {
            updateStateService.updateState(
                UserId(it.callback.user.userId),
                OrthoState.InputText(UserId(it.callback.user.userId), it.callback)
            )
            inputDoesntWorkText() prepareReplacementCurrentMessage AnswerParams(
                CallbackId(it.callback.callbackId)
            ) answerWith createBackButtonInlineKeyboard()
        }

        answerOnCallback(Payload(Payloads.TRANSLATE_EN)) {
            updateStateService.updateState(
                UserId(it.callback.user.userId),
                TranslateState.TranslateEn(UserId(it.callback.user.userId), it.callback)
            )
            inputTranslateText("английский") prepareReplacementCurrentMessage AnswerParams(
                CallbackId(it.callback.callbackId)
            ) answerWith createBackButtonInlineKeyboard()
        }

        answerOnCallback(Payload(Payloads.TRANSLATE_RU)) {
            updateStateService.updateState(
                UserId(it.callback.user.userId),
                TranslateState.TranslateRu(UserId(it.callback.user.userId), it.callback)
            )
            inputTranslateText("русский") prepareReplacementCurrentMessage AnswerParams(
                CallbackId(it.callback.callbackId)
            ) answerWith createBackButtonInlineKeyboard()
        }
    }

    private fun MessagesScope.answerOnMessages() {
        answerOnMessage {
            val updateState = updateStateService.selectState(UserId(it.message.sender.userId))
            val startTyping: suspend (ChatId) -> Unit = { id -> typingOn(id) }
            val stopTyping: suspend (ChatId) -> Unit = { id -> typingOff(id) }
            val sendFunction: suspend (String) -> ResultRequest<SendMessage> =
                { text -> text sendFor UserId(it.message.sender.userId) }
            val clearKeyboard: suspend (CallbackId) -> ResultRequest<Default> = { id -> "" answerFor id }
            when (updateState) {
                is TranslateState.TranslateEn -> messageUpdateLogic.translate(
                    "en",
                    it,
                    updateState.callback,
                    startTyping,
                    stopTyping,
                    sendFunction,
                    clearKeyboard
                )
                is TranslateState.TranslateRu -> messageUpdateLogic.translate(
                    "ru",
                    it,
                    updateState.callback,
                    startTyping,
                    stopTyping,
                    sendFunction,
                    clearKeyboard
                )
                is DictionaryState.InputWord -> messageUpdateLogic.processInputWord(
                    it,
                    updateState.callback,
                    startTyping,
                    stopTyping,
                    sendFunction,
                    clearKeyboard
                )
            }
        }

    }

}