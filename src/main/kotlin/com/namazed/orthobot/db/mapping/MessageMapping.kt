package com.namazed.orthobot.db.mapping

import chat.tamtam.botsdk.model.response.Message
import chat.tamtam.botsdk.model.response.MessageInfo
import chat.tamtam.botsdk.model.response.Recipient
import chat.tamtam.botsdk.model.response.User
import com.namazed.orthobot.db.model.UpdateStates
import org.jetbrains.exposed.sql.ResultRow

fun messageMapping(row: ResultRow) =
    Message(messageInfoMapping(row), recipientMapping(row), senderMapping(row), row[UpdateStates.timestamp])

private fun messageInfoMapping(row: ResultRow): MessageInfo {
    return MessageInfo(text = row[UpdateStates.messageInfoText])
}

private fun recipientMapping(row: ResultRow) = Recipient(row[UpdateStates.messageRecipientChatId])

private fun senderMapping(row: ResultRow) = User(row[UpdateStates.messageSenderId], row[UpdateStates.messageSenderName])