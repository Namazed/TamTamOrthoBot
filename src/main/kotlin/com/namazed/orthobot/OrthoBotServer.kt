@file:JvmName("OrthoBotServer")
package com.namazed.orthobot

import com.namazed.amspacebackend.client.botModule
import com.namazed.amspacebackend.client.clientModule
import com.namazed.orthobot.bot.BotHttpClientManager
import com.namazed.orthobot.bot.UpdateState
import com.namazed.orthobot.bot.model.UserId
import com.namazed.orthobot.bot.model.request.createMessage
import com.namazed.orthobot.bot.model.response.Message
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
    GlobalScope.launch {
        while (true) {
            val updates = withContext(httpClientManager.clientDispatcher) {
                httpClientManager.getUpdates()
            }
            try {
                processUpdates(updates) { message: Message ->
                    httpClientManager.sendMessage(UserId(message.sender.userId),
                        createMessage(UpdateState.StartState(UserId(message.sender.userId), message)))
                }
            } catch (e: Exception) {
                log.error("Exception when processUpdate", e)
            }
        }
    }
}
