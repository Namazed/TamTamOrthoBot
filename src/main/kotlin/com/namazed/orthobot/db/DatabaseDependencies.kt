package com.namazed.orthobot.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.Executors


val databaseModule = module {
    single { createHikariDataSource() }
    single<CoroutineDispatcher>(named("databaseDispatcher")) {
        Executors.newFixedThreadPool(5).asCoroutineDispatcher()
    }
    single { DatabaseManager(get(), get(named("databaseDispatcher"))) }
    single { Job() }
    single { UpdateStateService(get(), get()) }
}

private fun createHikariDataSource(): HikariDataSource {
    val config = HikariConfig().apply {
        driverClassName = "org.h2.Driver"
        jdbcUrl = "jdbc:h2:~/db"
        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }
    return HikariDataSource(config)
}
