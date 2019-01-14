package com.namazed.orthobot.bot

import com.namazed.orthobot.bot.model.UserId
import com.namazed.orthobot.bot.model.request.SendMessage
import com.namazed.orthobot.bot.model.response.Message

sealed class UpdateState {
    class StartState(val userId: UserId, val message: Message) : UpdateState()
    class OrthoState(val userId: UserId, val message: Message) : UpdateState()
    class DictionaryState(val userId: UserId, val message: Message) : UpdateState()
}

sealed class RequestState {
    class StartState(val userId: UserId, val sendMessage: SendMessage) : RequestState()
    class OrthoState(val userId: UserId, val sendMessage: SendMessage) : RequestState()
    class DictionaryState(val userId: UserId, val sendMessage: SendMessage) : RequestState()
}