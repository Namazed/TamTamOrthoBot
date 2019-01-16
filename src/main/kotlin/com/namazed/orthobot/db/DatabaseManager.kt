package com.namazed.orthobot.db

import com.namazed.orthobot.db.model.MessagesForEdit
import com.namazed.orthobot.db.model.UpdateStates
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction


class DatabaseManager(hikariDataSource: HikariDataSource, val dispatcher: CoroutineDispatcher) {

    init {
        open(hikariDataSource)
    }

    private fun open(hikariDataSource: HikariDataSource) {
        Database.connect(hikariDataSource)
        transaction {
            create(UpdateStates)
            create(MessagesForEdit)
        }
    }

    suspend inline fun <T> query(crossinline block: () -> T): T =
            withContext(dispatcher) {
                transaction { block() }
            }

}
