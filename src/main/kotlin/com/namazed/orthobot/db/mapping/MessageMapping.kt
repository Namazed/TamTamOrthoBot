package com.namazed.orthobot.db.mapping

import chat.tamtam.botsdk.model.ChatId
import chat.tamtam.botsdk.model.MessageId
import chat.tamtam.botsdk.model.UserId
import chat.tamtam.botsdk.model.prepared.Message
import chat.tamtam.botsdk.model.prepared.Body
import chat.tamtam.botsdk.model.prepared.Recipient
import chat.tamtam.botsdk.model.prepared.User
import chat.tamtam.botsdk.model.response.ChatType
import com.namazed.orthobot.db.model.UpdateStates
import org.jetbrains.exposed.sql.ResultRow

fun messageMapping(row: ResultRow) =
    Message(messageInfoMapping(row), recipientMapping(row), senderMapping(row), row[UpdateStates.timestamp], null)

private fun messageInfoMapping(row: ResultRow): Body {
    return Body(MessageId(""), -1, emptyList(), row[UpdateStates.messageInfoText])
}

private fun recipientMapping(row: ResultRow) = Recipient(ChatId(row[UpdateStates.messageRecipientChatId]),
    ChatType.UNKNOWN, UserId(-1))

private fun senderMapping(row: ResultRow) = User(UserId(row[UpdateStates.messageSenderId]), row[UpdateStates.messageSenderName],
    "", "", "")