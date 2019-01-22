package com.namazed.orthobot.client

import com.namazed.orthobot.ApiKeys

class ProdApiKeys(
    override val dictionaryApi: String = System.getenv("DIRY"),
    override val translateApi: String = System.getenv("TLTE"),
    override val botApi: String = System.getenv("BOT")
) : ApiKeys