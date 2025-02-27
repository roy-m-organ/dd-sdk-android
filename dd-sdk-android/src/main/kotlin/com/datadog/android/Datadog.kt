/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.configuration.Credentials
import com.datadog.android.core.internal.CoreFeature
import com.datadog.android.core.internal.lifecycle.ProcessLifecycleCallback
import com.datadog.android.core.internal.lifecycle.ProcessLifecycleMonitor
import com.datadog.android.core.internal.utils.devLogger
import com.datadog.android.core.internal.utils.warnDeprecated
import com.datadog.android.core.model.UserInfo
import com.datadog.android.error.internal.CrashReportsFeature
import com.datadog.android.log.internal.LogsFeature
import com.datadog.android.monitoring.internal.InternalLogsFeature
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.internal.RumFeature
import com.datadog.android.tracing.internal.TracesFeature
import java.lang.IllegalArgumentException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class initializes the Datadog SDK, and sets up communication with the server.
 */
@Suppress("TooManyFunctions")
object Datadog {

    internal val initialized = AtomicBoolean(false)
    internal val startupTimeNs: Long = System.nanoTime()

    internal var libraryVerbosity = Int.MAX_VALUE
        private set
    internal var isDebug = false

    // region Initialization

    /**
     * Initializes the Datadog SDK.
     * @param context your application context
     * @param config the configuration for the SDK library
     * @see [DatadogConfig]
     * @throws IllegalArgumentException if the env name is using illegal characters and your
     * application is in debug mode otherwise returns false and stops initializing the SDK
     * @deprecated Use the [Datadog.initialize] instead which requires
     * a privacy [TrackingConsent] parameter.
     */
    @Deprecated(
        "This method is deprecated and uses the [TrackingConsent.GRANTED] " +
            "flag as a default privacy consent.This means that the SDK will start recording " +
            "and sending data immediately after initialisation without waiting " +
            "for the user's consent to be tracked.",
        ReplaceWith(
            expression = "Datadog.initialize(context, TrackingConsent.PENDING, config)",
            imports = ["com.datadog.android.privacy.TrackingConsent"]
        )
    )
    @Suppress("LongMethod", "DEPRECATION")
    @JvmStatic
    fun initialize(
        context: Context,
        config: DatadogConfig
    ) {
        warnDeprecated(
            "Method Datadog.initialize(Context, DatadogConfig)",
            "1.7.0",
            "1.9.0",
            "Datadog.initialize(Context, TrackingConsent, DatadogConfig)"
        )
        initialize(context, TrackingConsent.GRANTED, config)
    }

    /**
     * Initializes the Datadog SDK.
     * @param context your application context
     * @param trackingConsent as the initial state of the tracking consent flag.
     * @param config the configuration for the SDK library
     * @see [DatadogConfig]
     * @see [TrackingConsent]
     * @throws IllegalArgumentException if the env name is using illegal characters and your
     * application is in debug mode otherwise returns false and stops initializing the SDK
     */
    @Deprecated(
        "This method is deprecated, as it doesn't take the application's flavor into account.",
        ReplaceWith(
            "initialize(context, credentials, configuration, trackingConsent)",
            "com.datadog.android.Datadog.initialize"
        )
    )
    @Suppress("LongMethod", "DEPRECATION")
    @JvmStatic
    fun initialize(
        context: Context,
        trackingConsent: TrackingConsent,
        config: DatadogConfig
    ) {
        initialize(
            context,
            config.asCredentials(),
            config.asConfiguration(),
            trackingConsent
        )
    }

    /**
     * Initializes the Datadog SDK.
     * @param context your application context
     * @param credentials your organization credentials
     * @param configuration the configuration for the SDK library
     * @param trackingConsent as the initial state of the tracking consent flag.
     * @see [DatadogConfig]
     * @see [TrackingConsent]
     * @throws IllegalArgumentException if the env name is using illegal characters and your
     * application is in debug mode otherwise returns false and stops initializing the SDK
     */
    @Suppress("LongMethod")
    @JvmStatic
    fun initialize(
        context: Context,
        credentials: Credentials,
        configuration: Configuration,
        trackingConsent: TrackingConsent
    ) {
        if (initialized.get()) {
            devLogger.w(MESSAGE_ALREADY_INITIALIZED)
            return
        }

        val appContext = context.applicationContext
        // the logic in this function depends on this value so always resolve isDebug first
        isDebug = resolveIsDebug(context)

        if (!validateEnvironmentName(credentials.envName)) {
            return
        }

        // always initialize Core Features first
        CoreFeature.initialize(appContext, credentials, configuration.coreConfig, trackingConsent)

        initializeLogsFeature(configuration.logsConfig, appContext)
        initializeTracingFeature(configuration.tracesConfig, appContext)
        initializeRumFeature(configuration.rumConfig, appContext)
        initializeCrashReportFeature(configuration.crashReportConfig, appContext)
        initializeInternalLogsFeature(configuration.internalLogsConfig, appContext)

        applyAdditionalConfiguration(configuration.additionalConfig)

        CoreFeature.ndkCrashHandler.handleNdkCrash(
            LogsFeature.persistenceStrategy.getWriter(),
            RumFeature.persistenceStrategy.getWriter()
        )

        setupLifecycleMonitorCallback(appContext)

        initialized.set(true)

        // Issue #154 (“Thread starting during runtime shutdown”)
        // Make sure we stop Datadog when the Runtime shuts down
        Runtime.getRuntime()
            .addShutdownHook(
                Thread(Runnable { stop() }, SHUTDOWN_THREAD)
            )
    }

    /**
     * Checks if the Datadog SDK was already initialized.
     * @return true if the SDK was initialized, false otherwise
     */
    @JvmStatic
    fun isInitialized(): Boolean {
        return initialized.get()
    }

    // endregion

    // region Global methods

    /**
     * Clears all data that has not already been sent to Datadog servers.
     */
    @JvmStatic
    fun clearAllData() {
        LogsFeature.clearAllData()
        CrashReportsFeature.clearAllData()
        RumFeature.clearAllData()
        TracesFeature.clearAllData()
        InternalLogsFeature.clearAllData()
    }

    // Stop all Datadog work (for test purposes).
    @Suppress("unused")
    private fun stop() {
        if (initialized.get()) {
            LogsFeature.stop()
            TracesFeature.stop()
            RumFeature.stop()
            CrashReportsFeature.stop()
            CoreFeature.stop()
            InternalLogsFeature.stop()
            isDebug = false
            initialized.set(false)
        }
    }

    // Executes all the pending queues in the upload/persistence executors.
    // Tries to send all the granted data for each feature and then clears the folders and shuts
    // down the persistence and the upload executors.
    // You should not use this method in production code. By calling this method you will basically
    // stop the SDKs persistence - upload streams and will leave it in an inconsistent state. This
    // method is mainly for test purposes.
    @Suppress("unused")
    private fun flushAndShutdownExecutors() {
        if (initialized.get()) {
            // We need to drain and shutdown the executors first to make sure we avoid duplicated
            // data due to async operations.
            CoreFeature.drainAndShutdownExecutors()
            LogsFeature.flushStoredData()
            TracesFeature.flushStoredData()
            RumFeature.flushStoredData()
            CrashReportsFeature.flushStoredData()
        }
    }

    /**
     * Sets the verbosity of the Datadog library.
     *
     * Messages with a priority level equal or above the given level will be sent to Android's
     * Logcat.
     *
     * @param level one of the Android [android.util.Log] constants
     * ([android.util.Log.VERBOSE], [android.util.Log.DEBUG], [android.util.Log.INFO],
     * [android.util.Log.WARN], [android.util.Log.ERROR], [android.util.Log.ASSERT]).
     */
    @JvmStatic
    fun setVerbosity(level: Int) {
        libraryVerbosity = level
    }

    /**
     * Sets the tracking consent regarding the data collection for the Datadog library.
     *
     * @param consent which can take one of the values
     * ([TrackingConsent.PENDING], [TrackingConsent.GRANTED], [TrackingConsent.NOT_GRANTED])
     */
    @JvmStatic
    fun setTrackingConsent(consent: TrackingConsent) {
        CoreFeature.trackingConsentProvider.setConsent(consent)
    }

    /**
     * Sets the user information.
     *
     * @param id (nullable) a unique user identifier (relevant to your business domain)
     * @param name (nullable) the user name or alias
     * @param email (nullable) the user email
     * @param extraInfo additional information. An extra information can be
     * nested up to 8 levels deep. Keys using more than 8 levels will be sanitized by SDK.
     */
    @JvmStatic
    @JvmOverloads
    fun setUserInfo(
        id: String? = null,
        name: String? = null,
        email: String? = null,
        extraInfo: Map<String, Any?> = emptyMap()
    ) {
        CoreFeature.userInfoProvider.setUserInfo(
            UserInfo(
                id,
                name,
                email,
                extraInfo
            )
        )
    }

    // endregion

    // region Internal Initialization

    private fun initializeLogsFeature(
        configuration: Configuration.Feature.Logs?,
        appContext: Context
    ) {
        if (configuration != null) {
            LogsFeature.initialize(appContext, configuration)
        }
    }

    private fun initializeCrashReportFeature(
        configuration: Configuration.Feature.CrashReport?,
        appContext: Context
    ) {
        if (configuration != null) {
            CrashReportsFeature.initialize(appContext, configuration)
        }
    }

    private fun initializeTracingFeature(
        configuration: Configuration.Feature.Tracing?,
        appContext: Context
    ) {
        if (configuration != null) {
            TracesFeature.initialize(appContext, configuration)
        }
    }

    private fun initializeRumFeature(
        configuration: Configuration.Feature.RUM?,
        appContext: Context
    ) {
        if (configuration != null) {
            if (CoreFeature.rumApplicationId.isNullOrBlank()) {
                devLogger.w(WARNING_MESSAGE_APPLICATION_ID_IS_NULL)
            }
            RumFeature.initialize(appContext, configuration)
        }
    }

    private fun initializeInternalLogsFeature(
        configuration: Configuration.Feature.InternalLogs?,
        appContext: Context
    ) {
        if (configuration != null) {
            InternalLogsFeature.initialize(appContext, configuration)
        }
    }

    private fun applyAdditionalConfiguration(
        @Suppress("UNUSED_PARAMETER") additionalConfiguration: Map<String, Any>
    ) {
        additionalConfiguration[DD_SOURCE_TAG]?.let {
            if (it.toString().isNotBlank()) {
                CoreFeature.sourceName = it.toString()
            }
        }
    }

    @Suppress("ThrowingInternalException")
    private fun validateEnvironmentName(envName: String): Boolean {
        if (!envName.matches(Regex(ENV_NAME_VALIDATION_REG_EX))) {
            if (isDebug) {
                throw IllegalArgumentException(MESSAGE_ENV_NAME_NOT_VALID)
            } else {
                devLogger.e(MESSAGE_ENV_NAME_NOT_VALID)
                return false
            }
        }
        return true
    }

    private fun setupLifecycleMonitorCallback(appContext: Context) {
        if (appContext is Application) {
            val callback = ProcessLifecycleCallback(CoreFeature.networkInfoProvider, appContext)
            appContext.registerActivityLifecycleCallbacks(ProcessLifecycleMonitor(callback))
        }
    }

    private fun resolveIsDebug(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    // endregion

    // region Constants

    internal const val MESSAGE_ALREADY_INITIALIZED =
        "The Datadog library has already been initialized."
    internal const val WARNING_MESSAGE_APPLICATION_ID_IS_NULL =
        "You're trying to enable RUM but no Application Id was provided. " +
            "Please pass this value into the Datadog Credentials:\n" +
            "val credentials = " +
            "Credentials" +
            "(\"<CLIENT_TOKEN>\", \"<ENVIRONMENT>\", \"<VARIANT>\", \"<APPLICATION_ID>\")\n" +
            "Datadog.initialize(context, credentials, configuration, trackingConsent);"

    internal const val MESSAGE_NOT_INITIALIZED = "Datadog has not been initialized.\n" +
        "Please add the following code in your application's onCreate() method:\n" +
        "val credentials = Credentials" +
        "(\"<CLIENT_TOKEN>\", \"<ENVIRONMENT>\", \"<VARIANT>\", \"<APPLICATION_ID>\")\n" +
        "Datadog.initialize(context, credentials, configuration, trackingConsent);"

    internal const val MESSAGE_DEPRECATED = "%s has been deprecated. " +
        "If you need it, submit an issue at https://github.com/DataDog/dd-sdk-android/issues/"

    internal const val SHUTDOWN_THREAD = "datadog_shutdown"
    internal const val ENV_NAME_VALIDATION_REG_EX = "[a-zA-Z0-9_:./-]{0,195}[a-zA-Z0-9_./-]"
    internal const val MESSAGE_ENV_NAME_NOT_VALID =
        "The environment name should contain maximum 196 of the following allowed characters " +
            "[a-zA-Z0-9_:./-] and should never finish with a semicolon." +
            "In this case the Datadog SDK will not be initialised."

    internal const val DD_SOURCE_TAG = "_dd.source"

    // endregion
}
