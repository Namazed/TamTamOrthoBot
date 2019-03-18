package com.namazed.orthobot.bot

import org.koin.dsl.module.module

val botModule = module {
    single { MessageUpdateLogic(get(), get(), getProperty("LOGGER")) }
    single { BotLogic(get(), get(), get(), getProperty("LOGGER")) }
}