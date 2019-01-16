package com.namazed.orthobot.db.mapping

import com.namazed.orthobot.bot.model.response.Callback
import com.namazed.orthobot.bot.model.response.User
import com.namazed.orthobot.db.model.UpdateStates
import org.jetbrains.exposed.sql.ResultRow

fun callbackMapping(row: ResultRow) =
    Callback(row[UpdateStates.timestamp], row[UpdateStates.callbackId], row[UpdateStates.callbackPayload], userMapping(row))

fun userMapping(row: ResultRow) = User(row[UpdateStates.callbackUserId], row[UpdateStates.callbackUserName])
