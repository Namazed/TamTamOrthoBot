@file:JvmName("OrthoBotServer")
package com.namazed.orthobot

import com.namazed.orthobot.bot.BotLogic
import com.namazed.orthobot.bot.botModule
import com.namazed.orthobot.client.clientModule
import com.namazed.orthobot.db.databaseModule
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import io.ktor.request.receive
import io.ktor.routing.get
import io.ktor.routing.routing
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject

fun Application.main() {
    install(CallLogging)
    install(Koin) {
        modules(listOf(serverModule, clientModule, botModule, databaseModule))
    }

    getKoin().setProperty("LOGGER", log)
    val botLogic: BotLogic by inject()

    routing {
        get("/updates") {
            val updates = call.receive<String>()
            botLogic.parseUpdates(updates)
        }
    }

    botLogic.start(true)
}
