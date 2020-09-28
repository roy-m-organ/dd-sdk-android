/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sample.data.db

import android.content.Context
import com.datadog.android.sample.data.model.Log
import com.datadog.android.sample.data.model.LogAttributes
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class LocalDataSource(val context: Context) {

    private val logsDatabase = Database.getInstance(context)

    // region LocalDataSource

    fun persistLogs(logs: List<Log>) {
        insertLogs(logs)
    }

    fun fetchLogs(): Single<List<Log>> {
        return Single.fromCallable(fetchLogsCallable).subscribeOn(Schedulers.io())
    }

    // endregion

    // region Internal

    private fun insertLogs(logs: List<Log>) {
        val currentTimeInMillis = System.currentTimeMillis()
        val minTtlRequired = currentTimeInMillis - LOGS_EXPIRING_TTL_IN_MS
        // purge data first
        logsDatabase.logsQueries.purgeLogs(minTtlRequired)
        // add new data
        logsDatabase.logsQueries.transaction {
            logs.forEach {
                logsDatabase.logsQueries.insertLog(
                    it.id,
                    it.attributes.message,
                    it.attributes.timestamp,
                    currentTimeInMillis
                )
            }
        }
    }

    private val fetchLogsCallable = Callable<List<Log>> {
        val minTtlRequired =
            System.currentTimeMillis() - LOGS_EXPIRING_TTL_IN_MS
        logsDatabase.logsQueries.getLogs(minTtlRequired).executeAsList().map {
            Log(
                it._id,
                attributes = LogAttributes(it.message ?: "", it.timestamp ?: "")
            )
        }
    }

    // endregion

    companion object {
        val LOGS_EXPIRING_TTL_IN_MS = TimeUnit.HOURS.toMillis(2)
    }
}