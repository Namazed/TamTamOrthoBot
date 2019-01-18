package com.namazed.orthobot.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module.module

val clientModule = module {
    factory(name = "Client") { createHttpClient() }
    single { HttpClientManager(get(name = "Client"), get()) }
}

fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
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
        serializer = GsonSerializer()
    }
}
