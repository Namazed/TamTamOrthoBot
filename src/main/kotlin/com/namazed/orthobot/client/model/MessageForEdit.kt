package com.namazed.orthobot.client.model

import com.namazed.orthobot.bot.model.MessageId

val EMPTY_MESSAGE_FOR_EDIT = MessageForEdit()

class MessageForEdit(
    val messageId: MessageId = MessageId(""),
    val text: String = ""
)