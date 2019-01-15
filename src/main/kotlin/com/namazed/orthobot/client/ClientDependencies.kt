package com.namazed.orthobot.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import kotlinx.coroutines.Dispatchers
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module.module
import kotlin.coroutines.CoroutineContext

val clientModule = module {
    factory(name = "Client") { createHttpClient() }
    single<CoroutineContext>(name = "clientDispatcher") { Dispatchers.IO }
    single { HttpClientManager(get(name = "Client"), get(), get("clientDispatcher")) }
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
