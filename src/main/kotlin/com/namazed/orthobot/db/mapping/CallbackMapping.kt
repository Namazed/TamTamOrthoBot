package com.namazed.orthobot.db.mapping

import chat.tamtam.botsdk.model.CallbackId
import chat.tamtam.botsdk.model.UserId
import chat.tamtam.botsdk.model.prepared.Callback
import chat.tamtam.botsdk.model.prepared.User
import com.namazed.orthobot.db.model.UpdateStates
import org.jetbrains.exposed.sql.ResultRow

fun callbackMapping(row: ResultRow) =
    Callback(row[UpdateStates.timestamp], CallbackId(row[UpdateStates.callbackId]), row[UpdateStates.callbackPayload], userMapping(row))

fun userMapping(row: ResultRow) = User(UserId(row[UpdateStates.callbackUserId]), row[UpdateStates.callbackUserName],
    "", "", "")
