@file:JvmName("OrthoBotServer")
package com.namazed.orthobot

import com.namazed.amspacebackend.client.botModule
import com.namazed.amspacebackend.client.clientModule
import com.namazed.orthobot.bot.BotHttpClientManager
import com.namazed.orthobot.bot.processUpdates
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.ktor.ext.inject
import org.koin.standalone.StandAloneContext.startKoin
import org.slf4j.Logger

fun Application.main() {
    startKoin(listOf(serverModule, clientModule, botModule))
    val botHttpClientManager: BotHttpClientManager by inject()

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()

            disableHtmlEscaping()
            disableInnerClassSerialization()
            enableComplexMapKeySerialization()

            serializeNulls()

            serializeSpecialFloatingPointValues()
            excludeFieldsWithoutExposeAnnotation()

            generateNonExecutableJson()

            setLenient()
            setVersion(0.0)
        }
    }


    GlobalScope.launch {
        val botInfo = withContext(botHttpClientManager.clientDispatcher) {
            botHttpClientManager.getBotInfo()
        }
    }

    startLongPolling(botHttpClientManager, log)
}

fun startLongPolling(httpClientManager: BotHttpClientManager, log: Logger) {
    while (true) {
        GlobalScope.launch {
            val updates = withContext(httpClientManager.clientDispatcher) {
                httpClientManager.getUpdates()
            }
            processUpdates(updates)
        }
    }
}
