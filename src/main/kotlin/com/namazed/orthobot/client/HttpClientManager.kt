package com.namazed.orthobot.client

import com.namazed.amspacebackend.client.model.Dictionary
import com.namazed.orthobot.ApiKeys
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import java.net.URL
import kotlin.coroutines.CoroutineContext

class HttpClientManager(
        private val httpClient: HttpClient,
        private val apiKeys: ApiKeys,
        val clientDispatcher: CoroutineContext
) {

    suspend fun loadDictionary(word: String) = httpClient.get<Dictionary> {
        url(URL("https://dictionary.yandex.net/api/v1/dicservice.json/lookup?"))
        parameter("key", apiKeys.dictionaryApi)
        parameter("lang", "ru-ru")
        parameter("text", word)
    }

}
