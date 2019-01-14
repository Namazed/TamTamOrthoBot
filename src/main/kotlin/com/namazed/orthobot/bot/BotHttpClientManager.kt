package com.namazed.orthobot.bot

import com.namazed.orthobot.ApiKeys
import com.namazed.orthobot.bot.model.BotInfo
import com.namazed.orthobot.bot.model.ChatId
import com.namazed.orthobot.bot.model.Updates
import com.namazed.orthobot.bot.model.UserId
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.url
import java.net.URL
import kotlin.coroutines.CoroutineContext

class BotHttpClientManager(
    private val httpClient: HttpClient,
    private val apiKeys: ApiKeys,
    val clientDispatcher: CoroutineContext
) {

    private val botEndpoint = "https://test2.tamtam.chat"

    suspend fun getBotInfo() = httpClient.get<BotInfo> {
        url(URL("$botEndpoint/me"))
        parameter("access_token", apiKeys.botApi)
    }

    suspend fun getUpdates() = httpClient.get<Updates> {
        url(URL("$botEndpoint/updates"))
        parameter("access_token", apiKeys.botApi)
    }

    suspend fun sendMessage(userId: UserId) = httpClient.put<Updates> {
        url(URL("$botEndpoint/messages"))
        parameter("access_token", apiKeys.botApi)
    }

    suspend fun sendMessage(chatId: ChatId) = httpClient.put<Updates> {
        url(URL("$botEndpoint/messages"))
        parameter("access_token", apiKeys.botApi)
    }

}