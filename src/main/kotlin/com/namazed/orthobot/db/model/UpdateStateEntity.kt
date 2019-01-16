package com.namazed.orthobot.db.model

import com.namazed.orthobot.db.UpdateTypes
import org.jetbrains.exposed.dao.IntIdTable

object UpdateStates : IntIdTable() {
    val userId = long("userId")
    val timestamp = long("timestamp")
    val dictionary = text("dictionary")
    val callbackId = text("callbackId")
    val callbackUserId = long("callbackUserId")
    val callbackUserName = text("callbackUserName")
    val callbackPayload = text("callbackPayload")
    val messageRecipientChatId = long("messageRecipientChatId")
    val messageSenderId = long("messageSenderId")
    val messageSenderName = text("messageSenderName")
    val messageInfoText = text("messageInfoText")
    val translationResultText = text("translationResultText")
    val translationResultLang = varchar("translationResultLang", 200)
    val updateTypes = enumeration("updateType", UpdateTypes::class)
}

object MessagesForEdit : IntIdTable() {
    val userId = long("userId")
    val messageId = text("messageId")
    val messageText = text("messageText")
}