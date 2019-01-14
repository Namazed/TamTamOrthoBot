package com.namazed.orthobot

import com.namazed.orthobot.client.ProdApiKeys
import org.koin.dsl.module.module

val serverModule = module {
    single<ApiKeys> { ProdApiKeys() }
}
