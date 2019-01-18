package com.namazed.orthobot.bot

import com.namazed.orthobot.ApiKeys
import com.namazed.orthobot.bot.model.CallbackId
import com.namazed.orthobot.bot.model.ChatId
import com.namazed.orthobot.bot.model.MessageId
import com.namazed.orthobot.bot.model.UserId
import com.namazed.orthobot.bot.model.response.BotInfo
import com.namazed.orthobot.bot.model.response.Updates
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.net.URL
import kotlin.coroutines.CoroutineContext
import com.namazed.orthobot.bot.model.request.AnswerCallback as RequestAnswerCallback
import com.namazed.orthobot.bot.model.request.SendMessage as RequestSendMessage
import com.namazed.orthobot.bot.model.response.AnswerCallback as ResponseAnswerCallback
import com.namazed.orthobot.bot.model.response.SendMessage as ResponseSendMessage

class BotHttpClientManager(
    private val httpClient: HttpClient,
    private val apiKeys: ApiKeys
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

    suspend fun sendMessage(userId: UserId, sendMessage: RequestSendMessage) = httpClient.post<ResponseSendMessage> {
        url(URL("$botEndpoint/messages"))
        parameter("access_token", apiKeys.botApi)
        parameter("user_id", userId.id)
        contentType(ContentType.parse("application/json"))
        body = sendMessage
    }

    suspend fun sendMessage(chatId: ChatId) = httpClient.post<ResponseSendMessage> {
        url(URL("$botEndpoint/messages"))
        parameter("access_token", apiKeys.botApi)
        parameter("chat_id", chatId.id)
        contentType(ContentType.parse("application/json"))
    }

    suspend fun editMessage(messageId: MessageId, sendMessage: RequestSendMessage) = httpClient.put<ResponseAnswerCallback> {
        url(URL("$botEndpoint/messages"))
        parameter("access_token", apiKeys.botApi)
        parameter("message_id", messageId.id)
        contentType(ContentType.parse("application/json"))
        body = sendMessage
    }

    suspend fun answerOnCallback(callbackId: CallbackId, answerCallback: RequestAnswerCallback) = httpClient.post<ResponseAnswerCallback> {
        url(URL("$botEndpoint/answers"))
        parameter("access_token", apiKeys.botApi)
        parameter("callback_id", callbackId.id)
        contentType(ContentType.parse("application/json"))
        body = answerCallback
    }

}