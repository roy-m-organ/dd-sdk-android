/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.log.internal

import android.content.Context
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.internal.CoreFeature
import com.datadog.android.core.internal.SdkFeature
import com.datadog.android.core.internal.net.DataUploader
import com.datadog.android.core.internal.persistence.PersistenceStrategy
import com.datadog.android.core.internal.utils.sdkLogger
import com.datadog.android.log.internal.domain.Log
import com.datadog.android.log.internal.domain.LogFilePersistenceStrategy
import com.datadog.android.log.internal.net.LogsOkHttpUploader

internal object LogsFeature : SdkFeature<Log, Configuration.Feature.Logs>() {

    // region SdkFeature

    override fun createPersistenceStrategy(
        context: Context,
        configuration: Configuration.Feature.Logs
    ): PersistenceStrategy<Log> {
        return LogFilePersistenceStrategy(
            CoreFeature.trackingConsentProvider,
            context,
            CoreFeature.persistenceExecutorService,
            sdkLogger
        )
    }

    override fun createUploader(configuration: Configuration.Feature.Logs): DataUploader {
        return LogsOkHttpUploader(
            configuration.endpointUrl,
            CoreFeature.clientToken,
            CoreFeature.okHttpClient
        )
    }

    // endregion
}
