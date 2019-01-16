package com.namazed.orthobot.bot

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module.module

val botModule = module {
    factory(name = "BotClient") { createBotHttpClient() }
    factory { CommandParser(get(), get(), getProperty("LOGGER")) }
    single { BotHttpClientManager(get(name = "BotClient"), get(), get("clientDispatcher")) }
}

fun createBotHttpClient(): HttpClient = HttpClient(OkHttp) {
    engine {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        addInterceptor(loggingInterceptor)
        config {
            followRedirects(true)
        }
    }

    install(JsonFeature) {
        serializer = GsonSerializer {
            serializeNulls()
            disableHtmlEscaping()
        }
    }
}
