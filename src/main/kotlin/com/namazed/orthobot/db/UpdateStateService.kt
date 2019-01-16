package com.namazed.orthobot.db

import com.namazed.orthobot.bot.*
import com.namazed.orthobot.bot.model.MessageId
import com.namazed.orthobot.bot.model.UserId
import com.namazed.orthobot.bot.model.response.Callback
import com.namazed.orthobot.bot.model.response.Message
import com.namazed.orthobot.bot.model.response.isNotEmptyCallback
import com.namazed.orthobot.bot.model.response.isNotEmptyMessage
import com.namazed.orthobot.client.model.EMPTY_MESSAGE_FOR_EDIT
import com.namazed.orthobot.client.model.MessageForEdit
import com.namazed.orthobot.client.model.TranslateResult
import com.namazed.orthobot.db.mapping.callbackMapping
import com.namazed.orthobot.db.mapping.mappingMessageForEdit
import com.namazed.orthobot.db.mapping.messageMapping
import com.namazed.orthobot.db.model.MessagesForEdit
import com.namazed.orthobot.db.model.UpdateStates
import com.namazed.orthobot.db.model.UpdateStates.actions
import com.namazed.orthobot.db.model.UpdateStates.callbackId
import com.namazed.orthobot.db.model.UpdateStates.callbackPayload
import com.namazed.orthobot.db.model.UpdateStates.callbackUserId
import com.namazed.orthobot.db.model.UpdateStates.callbackUserName
import com.namazed.orthobot.db.model.UpdateStates.dictionary
import com.namazed.orthobot.db.model.UpdateStates.messageInfoText
import com.namazed.orthobot.db.model.UpdateStates.messageRecipientChatId
import com.namazed.orthobot.db.model.UpdateStates.messageSenderId
import com.namazed.orthobot.db.model.UpdateStates.messageSenderName
import com.namazed.orthobot.db.model.UpdateStates.timestamp
import com.namazed.orthobot.db.model.UpdateStates.translationResultLang
import com.namazed.orthobot.db.model.UpdateStates.translationResultText
import com.namazed.orthobot.db.model.UpdateStates.updateTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import kotlin.coroutines.CoroutineContext

class UpdateStateService(
    private val databaseManager: DatabaseManager,
    val parentJob: Job,
    override val coroutineContext: CoroutineContext = SupervisorJob(parentJob) + Dispatchers.Default
) : CoroutineScope {

    suspend fun updateState(userId: UserId, updateState: UpdateState) {
        databaseManager.query {
            UpdateStates.deleteWhere { UpdateStates.userId eq userId.id }
            insertUpdateStates(updateState)
        }
    }

    suspend fun updateMessageForEdit(userId: UserId, messageId: MessageId, text: String?) {
        databaseManager.query {
            MessagesForEdit.deleteWhere { MessagesForEdit.userId eq userId.id }
            MessagesForEdit.insert {
                it[this.userId] = userId.id
                it[this.messageId] = messageId.id
                it[this.messageText] = text?.let { text: String -> text } ?: ""
            }
        }
    }

    suspend fun updateTextMessageForEdit(userId: UserId, text: String?) {
        databaseManager.query {
            MessagesForEdit.update({ MessagesForEdit.userId eq userId.id }) {
                it[this.messageText] = text?.let { text: String -> text } ?: ""
            }
        }
    }

    suspend fun selectMessageForEdit(userId: UserId): MessageForEdit = databaseManager.query {
        MessagesForEdit.select {
            MessagesForEdit.userId eq userId.id
        }.asSequence()
            .mapNotNull { mappingMessageForEdit(it) }
            .ifEmpty { listOf(EMPTY_MESSAGE_FOR_EDIT).asSequence() }
            .single()
    }


    private fun insertUpdateStates(updateState: UpdateState) {
        UpdateStates.insert {
            it[userId] = updateState.updateStateId.id
            when (updateState) {
                is StartState -> insertState(it, UpdateTypes.START, updateState.message, actions = updateState.actions)
                is BackState -> insertState(it, UpdateTypes.BACK, callback = updateState.callback)
                is DictionaryState.InputWord -> insertState(
                    it,
                    UpdateTypes.DICTIONARY_INPUT,
                    callback = updateState.callback
                )
                is DictionaryState.Result -> insertState(
                    it,
                    UpdateTypes.DICTIONARY_RESULT,
                    updateState.message,
                    updateState.dictionary
                )
                is TranslateState.TranslateEn -> insertState(
                    it,
                    UpdateTypes.TRANSLATE_EN,
                    callback = updateState.callback
                )
                is TranslateState.TranslateRu -> insertState(
                    it,
                    UpdateTypes.TRANSLATE_RU,
                    callback = updateState.callback
                )
                is TranslateState.Result ->
                    insertState(
                        it,
                        UpdateTypes.TRANSLATE_RESULT,
                        updateState.message,
                        translateResult = updateState.translateResult
                    )
            }
        }
    }

    private fun insertState(
        insertField: InsertStatement<Number>,
        updateType: UpdateTypes,
        message: Message = Message(),
        dictionaryResult: String = "",
        translateResult: TranslateResult = TranslateResult(),
        callback: Callback = Callback(),
        actions: Boolean = false
    ) {
        insertField[timestamp] = when {
            isNotEmptyMessage(message) -> message.timestamp
            isNotEmptyCallback(callback) -> callback.timestamp
            else -> -1L
        }
        insertField[dictionary] = dictionaryResult
        insertField[UpdateStates.actions] = actions
        insertField[messageSenderName] = message.sender.name
        insertField[messageSenderId] = message.sender.userId
        insertField[messageRecipientChatId] = message.recipient.chatId
        insertField[messageInfoText] = message.messageInfo.text
        insertField[translationResultLang] = translateResult.lang
        insertField[translationResultText] = if (translateResult.text.isEmpty()) "" else translateResult.text[0]
        insertField[callbackId] = callback.callbackId
        insertField[callbackUserId] = callback.user.userId
        insertField[callbackUserName] = callback.user.name
        insertField[callbackPayload] = callback.payload
        insertField[updateTypes] = updateType
    }

    suspend fun selectState(userId: UserId) = databaseManager.query {
        UpdateStates.select {
            (UpdateStates.userId eq userId.id)
        }.asSequence().mapNotNull { mapping(it) }.ifEmpty { listOf(StartState(UserId(-1), Message())).asSequence() }
            .single()
    }

    private fun mapping(row: ResultRow) = when (row[UpdateStates.updateTypes]) {
        UpdateTypes.START -> StartState(UserId(row[UpdateStates.userId]), messageMapping(row), row[UpdateStates.actions])
        UpdateTypes.BACK -> BackState(UserId(row[UpdateStates.userId]), callbackMapping(row))
        UpdateTypes.DICTIONARY_INPUT -> DictionaryState.InputWord(
            UserId(row[UpdateStates.userId]),
            callbackMapping(row)
        )
        UpdateTypes.DICTIONARY_RESULT -> DictionaryState.Result(
            UserId(row[UpdateStates.userId]),
            message = messageMapping(row),
            dictionary = row[UpdateStates.dictionary]
        )
        UpdateTypes.TRANSLATE_EN -> TranslateState.TranslateEn(
            UserId(row[UpdateStates.userId]),
            callbackMapping(row)
        )
        UpdateTypes.TRANSLATE_RU -> TranslateState.TranslateRu(
            UserId(row[UpdateStates.userId]),
            callbackMapping(row)
        )
        UpdateTypes.TRANSLATE_RESULT -> TranslateState.Result(
            UserId(row[UpdateStates.userId]),
            message = messageMapping(row),
            translateResult = TranslateResult(
                200,
                row[UpdateStates.translationResultLang],
                listOf(row[UpdateStates.translationResultText])
            )
        )
        else -> Unknown()
    }

}

enum class UpdateTypes {
    START,
    BACK,
    ORTHO_INPUT,
    ORTHO_INPUT_COMMAND,
    ORTHO_RESULT,
    DICTIONARY_INPUT,
    DICTIONARY_INPUT_COMMAND,
    DICTIONARY_RESULT,
    TRANSLATE_EN,
    TRANSLATE_RU,
    TRANSLATE_RESULT
}
