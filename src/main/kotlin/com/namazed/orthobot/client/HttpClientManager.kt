package com.namazed.orthobot.client

import com.namazed.orthobot.ApiKeys
import com.namazed.orthobot.client.model.CheckResult
import com.namazed.orthobot.client.model.Dictionary
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
        parameter("ui", "ru")
        parameter("text", word)
    }

    suspend fun spellCheck(text: String) = httpClient.get<List<CheckResult>> {
        url(URL("https://speller.yandex.net/services/spellservice.json/checkText?"))
        parameter("lang", "ru-ru")
        parameter("text", text)
    }

//    suspend fun translateRuEn(text: String) = httpClient.get<> {
//        url(URL("https://translate.yandex.net/api/v1.5/tr.json/translate?"))
//        parameter("key", )
//    }

}
