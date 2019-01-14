package com.namazed.orthobot.bot

import com.namazed.orthobot.bot.model.response.Message
import com.namazed.orthobot.bot.model.response.SendMessage
import com.namazed.orthobot.bot.model.response.Updates
import com.namazed.orthobot.bot.model.response.isEmptyMessage

private val PROFILE_TAG_PATTERN = Regex("@([A-Za-z0-9_-]+)")
private val START_COMMAND_PATTERN = Regex("(?i)/start")
private val BOTS_COMMAND_PATTERN = Regex("/[\\p{L}\\p{N}_]+($PROFILE_TAG_PATTERN)?")

fun isStartCommand(text: String) = START_COMMAND_PATTERN.matches(text)

fun isUnknownCommand(text: String) = BOTS_COMMAND_PATTERN.matches(text)

inline fun processUpdates(
    updates: Updates,
    startRequest: (Message) -> SendMessage
) {
    updates.listUpdates.forEach {
        when {
            !isEmptyMessage(it.message) && isStartCommand(it.message.messageInfo.text) -> startRequest(it.message)
//            !it.callback.isEmpty() && it.callback.
        }
    }
}