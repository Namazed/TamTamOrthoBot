package com.namazed.orthobot.bot

import chat.tamtam.botsdk.model.CallbackId
import chat.tamtam.botsdk.model.UserId
import chat.tamtam.botsdk.model.prepared.Callback
import chat.tamtam.botsdk.model.prepared.Message
import com.namazed.orthobot.client.model.TranslateResult

sealed class UpdateState(val updateStateId: UserId)

class Unknown : UpdateState(UserId(-1))

class StartState(val userId: UserId, val message: Message? = null, val actions: Boolean = false) : UpdateState(userId)

class BackState(val userId: UserId, val callback: Callback) : UpdateState(userId)

class ClearState(val userId: UserId, val messageText: String) : UpdateState(userId)

sealed class OrthoState(val orthoId: UserId) : UpdateState(orthoId) {
    class InputText(val userId: UserId, val callback: Callback) : OrthoState(userId)
    class InputTextCommand(val userId: UserId, val message: Message) : OrthoState(userId)
    class Result(
        val userId: UserId,
        val callbackId: CallbackId = CallbackId(""),
        val message: Message
    ) : OrthoState(userId)
}

sealed class DictionaryState(val dictionaryId: UserId) : UpdateState(dictionaryId) {
    class InputWord(val userId: UserId, val callback: Callback) : DictionaryState(userId)
    class InputWordCommand(val userId: UserId, val message: Message) : DictionaryState(userId)
    class Result(
        val userId: UserId,
        val callbackId: CallbackId = CallbackId(""),
        val message: Message,
        val dictionary: String
    ) : DictionaryState(userId)
}

sealed class TranslateState(val translateId: UserId) : UpdateState(translateId) {
    class TranslateEn(val userId: UserId, val callback: Callback) : TranslateState(userId)
    class TranslateRu(val userId: UserId, val callback: Callback) : TranslateState(userId)
    class Result(
        val userId: UserId,
        val callbackId: CallbackId = CallbackId(""),
        val message: Message,
        val translateResult: TranslateResult
    ) : TranslateState(userId)
}