/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.rum.internal.domain.scope

import com.datadog.android.core.internal.CoreFeature
import com.datadog.android.core.internal.persistence.DataWriter
import com.datadog.android.core.internal.utils.devLogger
import com.datadog.android.rum.GlobalRum
import com.datadog.android.rum.RumActionType
import com.datadog.android.rum.internal.domain.RumContext
import com.datadog.android.rum.internal.domain.Time
import com.datadog.android.rum.internal.domain.event.RumEvent
import com.datadog.android.rum.model.ActionEvent
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.max

internal class RumActionScope(
    val parentScope: RumScope,
    val waitForStop: Boolean,
    eventTime: Time,
    initialType: RumActionType,
    initialName: String,
    initialAttributes: Map<String, Any?>,
    inactivityThresholdMs: Long = ACTION_INACTIVITY_MS,
    maxDurationMs: Long = ACTION_MAX_DURATION_MS
) : RumScope {

    private val inactivityThresholdNs = TimeUnit.MILLISECONDS.toNanos(inactivityThresholdMs)
    private val maxDurationNs = TimeUnit.MILLISECONDS.toNanos(maxDurationMs)

    private val eventTimestamp = eventTime.timestamp
    internal val actionId: String = UUID.randomUUID().toString()
    internal var type: RumActionType = initialType
    internal var name: String = initialName
    private val startedNanos: Long = eventTime.nanoTime
    private var lastInteractionNanos: Long = startedNanos

    internal val attributes: MutableMap<String, Any?> = initialAttributes.toMutableMap()

    private val ongoingResourceKeys = mutableListOf<WeakReference<Any>>()

    internal var resourceCount: Long = 0
    internal var errorCount: Long = 0
    internal var crashCount: Long = 0
    internal var longTaskCount: Long = 0
    internal var viewTreeChangeCount: Int = 0

    private var sent = false
    private var stopped = false

    // endregion

    override fun handleEvent(event: RumRawEvent, writer: DataWriter<RumEvent>): RumScope? {
        val now = event.eventTime.nanoTime
        val isInactive = now - lastInteractionNanos > inactivityThresholdNs
        val isLongDuration = now - startedNanos > maxDurationNs
        ongoingResourceKeys.removeAll { it.get() == null }
        val isOngoing = waitForStop && !stopped
        val shouldStop = isInactive && ongoingResourceKeys.isEmpty() && !isOngoing

        when {
            shouldStop -> sendAction(lastInteractionNanos, writer)
            isLongDuration -> sendAction(now, writer)
            event is RumRawEvent.SendCustomActionNow -> sendAction(lastInteractionNanos, writer)
            event is RumRawEvent.ViewTreeChanged -> onViewTreeChanged(now)
            event is RumRawEvent.StopView -> onStopView(now, writer)
            event is RumRawEvent.StopAction -> onStopAction(event, now)
            event is RumRawEvent.StartResource -> onStartResource(event, now)
            event is RumRawEvent.StopResource -> onStopResource(event, now)
            event is RumRawEvent.AddError -> onError(event, now, writer)
            event is RumRawEvent.StopResourceWithError -> onResourceError(event, now)
            event is RumRawEvent.AddLongTask -> onLongTask(now)
        }

        return if (sent) null else this
    }

    override fun getRumContext(): RumContext {
        return parentScope.getRumContext()
    }

    // endregion

    // region Internal

    private fun onViewTreeChanged(now: Long) {
        lastInteractionNanos = now
        viewTreeChangeCount++
    }

    private fun onStopView(
        now: Long,
        writer: DataWriter<RumEvent>
    ) {
        ongoingResourceKeys.clear()
        sendAction(now, writer)
    }

    private fun onStopAction(
        event: RumRawEvent.StopAction,
        now: Long
    ) {
        event.type?.let { type = it }
        event.name?.let { name = it }
        attributes.putAll(event.attributes)
        stopped = true
        lastInteractionNanos = now
    }

    private fun onStartResource(
        event: RumRawEvent.StartResource,
        now: Long
    ) {
        lastInteractionNanos = now
        resourceCount++
        ongoingResourceKeys.add(WeakReference(event.key))
    }

    private fun onStopResource(
        event: RumRawEvent.StopResource,
        now: Long
    ) {
        val keyRef = ongoingResourceKeys.firstOrNull { it.get() == event.key }
        if (keyRef != null) {
            ongoingResourceKeys.remove(keyRef)
            lastInteractionNanos = now
        }
    }

    private fun onError(
        event: RumRawEvent.AddError,
        now: Long,
        writer: DataWriter<RumEvent>
    ) {
        lastInteractionNanos = now
        errorCount++

        if (event.isFatal) {
            crashCount++

            sendAction(now, writer)
        }
    }

    private fun onResourceError(event: RumRawEvent.StopResourceWithError, now: Long) {
        val keyRef = ongoingResourceKeys.firstOrNull { it.get() == event.key }
        if (keyRef != null) {
            ongoingResourceKeys.remove(keyRef)
            lastInteractionNanos = now
            resourceCount--
            errorCount++
        }
    }

    private fun onLongTask(now: Long) {
        lastInteractionNanos = now
        longTaskCount++
    }

    private fun sendAction(
        endNanos: Long,
        writer: DataWriter<RumEvent>
    ) {
        if (sent) return

        val actualType = type
        if (actionCanBeSent(actualType)) {
            attributes.putAll(GlobalRum.globalAttributes)

            val context = getRumContext()
            val user = CoreFeature.userInfoProvider.getUserInfo()

            val actionEvent = ActionEvent(
                date = eventTimestamp,
                action = ActionEvent.Action(
                    type = actualType.toSchemaType(),
                    id = actionId,
                    target = ActionEvent.Target(name),
                    error = ActionEvent.Error(errorCount),
                    crash = ActionEvent.Crash(crashCount),
                    longTask = ActionEvent.LongTask(longTaskCount),
                    resource = ActionEvent.Resource(resourceCount),
                    loadingTime = max(endNanos - startedNanos, 1L)
                ),
                view = ActionEvent.View(
                    id = context.viewId.orEmpty(),
                    url = context.viewUrl.orEmpty()
                ),
                application = ActionEvent.Application(context.applicationId),
                session = ActionEvent.Session(
                    id = context.sessionId,
                    type = ActionEvent.SessionType.USER
                ),
                usr = ActionEvent.Usr(
                    id = user.id,
                    name = user.name,
                    email = user.email
                ),
                dd = ActionEvent.Dd()
            )
            val rumEvent = RumEvent(
                event = actionEvent,
                globalAttributes = attributes,
                userExtraAttributes = user.additionalProperties
            )
            writer.write(rumEvent)
        } else {
            parentScope.handleEvent(
                RumRawEvent.ActionDropped(getRumContext().viewId.orEmpty()),
                writer
            )
            devLogger.i(
                "RUM Action $actionId ($actualType on $name) was dropped " +
                    "(no side effect was registered during its scope)"
            )
        }
        sent = true
    }

    private fun actionCanBeSent(actualType: RumActionType): Boolean {
        val sideEffectsCount = resourceCount + errorCount + viewTreeChangeCount + longTaskCount
        return sideEffectsCount > 0 || actualType == RumActionType.CUSTOM
    }

    // endregion

    companion object {
        internal const val ACTION_INACTIVITY_MS = 100L
        internal const val ACTION_MAX_DURATION_MS = 5000L

        fun fromEvent(
            parentScope: RumScope,
            event: RumRawEvent.StartAction
        ): RumScope {
            return RumActionScope(
                parentScope,
                event.waitForStop,
                event.eventTime,
                event.type,
                event.name,
                event.attributes
            )
        }
    }
}
