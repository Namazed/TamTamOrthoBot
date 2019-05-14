package com.namazed.orthobot.db.mapping

import chat.tamtam.botsdk.model.UserId
import com.namazed.orthobot.bot.*
import com.namazed.orthobot.client.model.TranslateResult
import com.namazed.orthobot.db.UpdateTypes
import com.namazed.orthobot.db.model.UpdateStates
import org.jetbrains.exposed.sql.ResultRow

fun updateStateMapping(row: ResultRow) = when (row[UpdateStates.updateTypes]) {
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

fun getDefaultState() = listOf(StartState(UserId(-1), null)).asSequence()