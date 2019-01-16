@file:JvmName("OrthoBotServer")
package com.namazed.orthobot

import com.namazed.orthobot.bot.BotHttpClientManager
import com.namazed.orthobot.bot.CommandParser
import com.namazed.orthobot.bot.botModule
import com.namazed.orthobot.bot.model.CallbackId
import com.namazed.orthobot.bot.model.MessageId
import com.namazed.orthobot.bot.model.UserId
import com.namazed.orthobot.bot.model.request.AnswerCallback
import com.namazed.orthobot.bot.model.request.SendMessage
import com.namazed.orthobot.bot.model.response.Updates
import com.namazed.orthobot.client.clientModule
import com.namazed.orthobot.db.databaseModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.ktor.ext.inject
import org.koin.ktor.ext.setProperty
import org.koin.standalone.StandAloneContext.startKoin
import org.slf4j.Logger

fun Application.main() {
    startKoin(listOf(serverModule, clientModule, botModule, databaseModule))
    setProperty("LOGGER", log)
    val botHttpClientManager: BotHttpClientManager by inject()
    val commandParser: CommandParser by inject()

    install(CallLogging)

    startLongPolling(botHttpClientManager, commandParser, log)
}

fun startLongPolling(httpClientManager: BotHttpClientManager, commandParser: CommandParser, log: Logger) {
    GlobalScope.launch {
        while (true) {
            val updates: Updates
            try {
                updates = withContext(httpClientManager.clientDispatcher) {
                               httpClientManager.getUpdates()
                           }
            } catch (e: Exception) {
                log.error("Exception when getUpdates", e)
                continue
            }
            try {
                commandParser.processUpdates(updates, { userId: UserId, sendMessage: SendMessage ->
                    log.info("Send message")
                    httpClientManager.sendMessage(userId, sendMessage)
                }, { callbackId: CallbackId, answerCallback: AnswerCallback ->
                    log.info("Answer on callback")
                    httpClientManager.answerOnCallback(callbackId, answerCallback)
                }, { messageId: MessageId, sendMessage: SendMessage ->
                    log.info("Clear last message with buttons, messageId = ${messageId.id}")
                    httpClientManager.editMessage(messageId, sendMessage)
                })
            } catch (e: Exception) {
                log.error("Exception when processUpdate", e)
            }
        }
    }
}
