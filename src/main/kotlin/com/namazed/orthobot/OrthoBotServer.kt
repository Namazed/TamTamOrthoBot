@file:JvmName("OrthoBotServer")
package com.namazed.orthobot

import com.namazed.orthobot.bot.BotLogic
import com.namazed.orthobot.bot.botModule
import com.namazed.orthobot.client.clientModule
import com.namazed.orthobot.db.databaseModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import org.koin.ktor.ext.inject
import org.koin.ktor.ext.setProperty
import org.koin.standalone.StandAloneContext.startKoin

fun Application.main() {
    startKoin(listOf(serverModule, clientModule, botModule, databaseModule))
    setProperty("LOGGER", log)
    val botLogic: BotLogic by inject()

    install(CallLogging)

    botLogic.start()
}
