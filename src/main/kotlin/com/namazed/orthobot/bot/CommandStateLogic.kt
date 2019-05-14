package com.namazed.orthobot.bot

import chat.tamtam.botsdk.scopes.BotScope
import chat.tamtam.botsdk.scopes.CommandsScope
import chat.tamtam.botsdk.state.CommandState
import chat.tamtam.botsdk.state.StartedBotState
import com.namazed.orthobot.bot.model.createAllActionsInlineKeyboard
import com.namazed.orthobot.db.UpdateStateService

suspend fun CommandsScope.handleCommandWithAllActions(
    state: CommandState,
    updateStateService: UpdateStateService,
    messageText: String
) {
    typingOn(state.command.message.recipient.chatId)
    val userId = state.command.message.sender.userId
    updateStateService.updateState(userId, StartState(userId))
    messageText prepareFor userId sendWith createAllActionsInlineKeyboard()
    typingOff(state.command.message.recipient.chatId)
}

suspend fun BotScope.handleCommandWithAllActions(
    state: StartedBotState,
    updateStateService: UpdateStateService,
    messageText: String
) {
    typingOn(state.chatId)
    val userId = state.userId
    updateStateService.updateState(userId, StartState(userId))
    messageText prepareFor userId sendWith createAllActionsInlineKeyboard()
    typingOff(state.chatId)
}