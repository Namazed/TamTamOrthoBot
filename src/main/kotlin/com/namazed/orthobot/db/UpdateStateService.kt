package com.namazed.orthobot.db

import chat.tamtam.botsdk.model.UserId
import chat.tamtam.botsdk.model.response.Callback
import chat.tamtam.botsdk.model.response.Message
import com.namazed.orthobot.bot.*
import com.namazed.orthobot.client.model.TranslateResult
import com.namazed.orthobot.db.mapping.getDefaultState
import com.namazed.orthobot.db.mapping.updateStateMapping
import com.namazed.orthobot.db.model.UpdateStates
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
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import kotlin.coroutines.CoroutineContext

class UpdateStateService(
    private val databaseManager: DatabaseManager,
    val parentJob: Job,
    override val coroutineContext: CoroutineContext = SupervisorJob(parentJob) + Dispatchers.IO
) : CoroutineScope {

    suspend fun updateState(userId: UserId, updateState: UpdateState): UpdateState {
        databaseManager.query {
            UpdateStates.deleteWhere { UpdateStates.userId eq userId.id }
            insertUpdateStates(updateState)
        }
        return selectState(userId)
    }

    private fun insertUpdateStates(updateState: UpdateState) {
        UpdateStates.insert {
            it[userId] = updateState.updateStateId.id
            when (updateState) {
                is StartState -> insertState(it, UpdateTypes.START, updateState.message, actions = updateState.actions)
                is BackState -> insertState(it, UpdateTypes.BACK, callback = updateState.callback)
                is DictionaryState.InputWord -> insertState(it, UpdateTypes.DICTIONARY_INPUT, callback = updateState.callback)
                is DictionaryState.Result -> insertState(it, UpdateTypes.DICTIONARY_RESULT, updateState.message, updateState.dictionary)
                is TranslateState.TranslateEn -> insertState(it, UpdateTypes.TRANSLATE_EN, callback = updateState.callback)
                is TranslateState.TranslateRu -> insertState(it, UpdateTypes.TRANSLATE_RU, callback = updateState.callback)
                is TranslateState.Result ->
                    insertState(it, UpdateTypes.TRANSLATE_RESULT, updateState.message, translateResult = updateState.translateResult)
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
            message != null && message.timestamp != -1L -> message.timestamp
            callback != null && callback.timestamp != -1L -> callback.timestamp
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
        }.asSequence().mapNotNull { updateStateMapping(it) }.ifEmpty { getDefaultState() }.single()
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
