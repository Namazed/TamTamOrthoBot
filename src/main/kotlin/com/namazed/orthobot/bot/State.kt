package com.namazed.orthobot.bot

import com.namazed.orthobot.bot.model.CallbackId
import com.namazed.orthobot.bot.model.UserId
import com.namazed.orthobot.bot.model.response.Callback
import com.namazed.orthobot.bot.model.response.Message
import com.namazed.orthobot.client.model.Dictionary
import com.namazed.orthobot.client.model.TranslateResult

sealed class UpdateState {
    class StartState(val userId: UserId, val message: Message) : UpdateState()
    class BackState(val userId: UserId, val callback: Callback) : UpdateState()
    sealed class OrthoState : UpdateState() {
        class InputText(val userId: UserId, val callback: Callback) : OrthoState()
        class InputTextCommand(val userId: UserId, val message: Message) : OrthoState()
        class Result(val userId: UserId, val callbackId: CallbackId = CallbackId(""), val message: Message) : OrthoState()
    }
    sealed class DictionaryState : UpdateState() {
        class InputWord(val userId: UserId, val callback: Callback) : DictionaryState()
        class InputWordCommand(val userId: UserId, val message: Message) : DictionaryState()
        class Result(val userId: UserId, val callbackId: CallbackId = CallbackId(""), val message: Message, val dictionary: Dictionary) : DictionaryState()
    }
    sealed class TranslateState: UpdateState() {
        class TranslateEn(val userId: UserId, val callback: Callback) : TranslateState()
        class TranslateRu(val userId: UserId, val callback: Callback) : TranslateState()
        class Result(val userId: UserId, val callbackId: CallbackId = CallbackId(""), val message: Message, val translateResult: TranslateResult) : TranslateState()
    }
}