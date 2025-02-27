/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.nightly.logs

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.datadog.android.log.Logger
import com.datadog.android.nightly.SPECIAL_ATTRIBUTE_NAME
import com.datadog.android.nightly.SPECIAL_BOOL_ATTRIBUTE_NAME
import com.datadog.android.nightly.SPECIAL_DATE_ATTRIBUTE_NAME
import com.datadog.android.nightly.SPECIAL_DOUBLE_ATTRIBUTE_NAME
import com.datadog.android.nightly.SPECIAL_FLOAT_ATTRIBUTE_NAME
import com.datadog.android.nightly.SPECIAL_INT_ATTRIBUTE_NAME
import com.datadog.android.nightly.SPECIAL_JSONARRAY_ATTRIBUTE_NAME
import com.datadog.android.nightly.SPECIAL_JSONOBJECT_ATTRIBUTE_NAME
import com.datadog.android.nightly.SPECIAL_STRING_ATTRIBUTE_NAME
import com.datadog.android.nightly.SPECIAL_TAG_NAME
import com.datadog.android.nightly.rules.NightlyTestRule
import com.datadog.android.nightly.utils.defaultTestAttributes
import com.datadog.android.nightly.utils.initializeSdk
import com.datadog.android.nightly.utils.measure
import com.datadog.tools.unit.forge.aThrowable
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import fr.xgouchet.elmyr.junit4.ForgeRule
import java.text.SimpleDateFormat
import java.util.Date
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoggerE2ETests {

    @get:Rule
    val forge = ForgeRule()

    @get:Rule
    val nightlyTestRule = NightlyTestRule()

    lateinit var logger: Logger

    @Before
    fun setUp() {
        initializeSdk(InstrumentationRegistry.getInstrumentation().targetContext)
        logger = Logger.Builder()
            .setLoggerName(LOGGER_NAME)
            .build()
    }

    /**
     * apiMethodSignature: Logger#fun v(String, Throwable? = null, Map<String, Any?> = emptyMap())
     */
    @Test
    fun logs_logger_verbose_log() {
        val testMethodName = "logs_logger_verbose_log"
        val fakeMessage = forge.anAlphaNumericalString()
        val attributes = defaultTestAttributes(testMethodName)
        measure(testMethodName) {
            logger.v(
                fakeMessage,
                null,
                attributes
            )
        }
    }

    /**
     * apiMethodSignature: Logger#fun v(String, Throwable? = null, Map<String, Any?> = emptyMap())
     */
    @Test
    fun logs_logger_verbose_log_with_error() {
        val testMethodName = "logs_logger_verbose_log_with_error"
        val fakeThrowable = forge.aThrowable()
        val fakeMessage = forge.anAlphaNumericalString()
        val attributes = defaultTestAttributes(testMethodName)
        measure(testMethodName) {
            logger.v(
                fakeMessage,
                fakeThrowable,
                attributes
            )
        }
    }

    /**
     * apiMethodSignature: Logger#fun d(String, Throwable? = null, Map<String, Any?> = emptyMap())
     */
    @Test
    fun logs_logger_debug_log() {
        val testMethodName = "logs_logger_debug_log"
        val fakeMessage = forge.anAlphaNumericalString()
        val attributes = defaultTestAttributes(testMethodName)
        measure(testMethodName) {
            logger.d(
                fakeMessage,
                null,
                attributes
            )
        }
    }

    /**
     * apiMethodSignature: Logger#fun d(String, Throwable? = null, Map<String, Any?> = emptyMap())
     */
    @Test
    fun logs_logger_debug_log_with_error() {
        val testMethodName = "logs_logger_debug_log_with_error"
        val fakeThrowable = forge.aThrowable()
        val fakeMessage = forge.anAlphaNumericalString()
        val attributes = defaultTestAttributes(testMethodName)
        measure(testMethodName) {
            logger.d(
                fakeMessage,
                fakeThrowable,
                attributes
            )
        }
    }

    /**
     * apiMethodSignature: Logger#fun i(String, Throwable? = null, Map<String, Any?> = emptyMap())
     */
    @Test
    fun logs_logger_info_log() {
        val testMethodName = "logs_logger_info_log"
        val fakeMessage = forge.anAlphaNumericalString()
        val attributes = defaultTestAttributes(testMethodName)
        measure(testMethodName) {
            logger.i(
                fakeMessage,
                null,
                attributes
            )
        }
    }

    /**
     * apiMethodSignature: Logger#fun i(String, Throwable? = null, Map<String, Any?> = emptyMap())
     */
    @Test
    fun logs_logger_info_log_with_error() {
        val testMethodName = "logs_logger_info_log_with_error"
        val fakeThrowable = forge.aThrowable()
        val fakeMessage = forge.anAlphaNumericalString()
        val attributes = defaultTestAttributes(testMethodName)
        measure(testMethodName) {
            logger.i(
                fakeMessage,
                fakeThrowable,
                attributes
            )
        }
    }

    /**
     * apiMethodSignature: Logger#fun e(String, Throwable? = null, Map<String, Any?> = emptyMap())
     */
    @Test
    fun logs_logger_error_log() {
        val testMethodName = "logs_logger_error_log"
        val fakeMessage = forge.anAlphaNumericalString()
        val attributes = defaultTestAttributes(testMethodName)
        measure(testMethodName) {
            logger.e(
                fakeMessage,
                null,
                attributes
            )
        }
    }

    /**
     * apiMethodSignature: Logger#fun e(String, Throwable? = null, Map<String, Any?> = emptyMap())
     */
    @Test
    fun logs_logger_error_log_with_error() {
        val testMethodName = "logs_logger_error_log_with_error"
        val fakeThrowable = forge.aThrowable()
        val fakeMessage = forge.anAlphaNumericalString()
        val attributes = defaultTestAttributes(testMethodName)
        measure(testMethodName) {
            logger.e(
                fakeMessage,
                fakeThrowable,
                attributes
            )
        }
    }

    /**
     * apiMethodSignature: Logger#fun w(String, Throwable? = null, Map<String, Any?> = emptyMap())
     */
    @Test
    fun logs_logger_warning_log() {
        val testMethodName = "logs_logger_warning_log"
        val fakeMessage = forge.anAlphaNumericalString()
        val attributes = defaultTestAttributes(testMethodName)
        measure(testMethodName) {
            logger.w(
                fakeMessage,
                null,
                attributes
            )
        }
    }

    /**
     * apiMethodSignature: Logger#fun w(String, Throwable? = null, Map<String, Any?> = emptyMap())
     */
    @Test
    fun logs_logger_warning_log_with_error() {
        val testMethodName = "logs_logger_warning_log_with_error"
        val fakeThrowable = forge.aThrowable()
        val fakeMessage = forge.anAlphaNumericalString()
        val attributes = defaultTestAttributes(testMethodName)
        measure(testMethodName) {
            logger.w(
                fakeMessage,
                fakeThrowable,
                attributes
            )
        }
    }

    /**
     * apiMethodSignature: Logger#fun wtf(String, Throwable? = null, Map<String, Any?> = emptyMap())
     */
    @Test
    fun logs_logger_wtf_log() {
        val testMethodName = "logs_logger_wtf_log"
        val fakeMessage = forge.anAlphaNumericalString()
        val attributes = defaultTestAttributes(testMethodName)
        measure(testMethodName) {
            logger.wtf(
                fakeMessage,
                null,
                attributes
            )
        }
    }

    /**
     * apiMethodSignature: Logger#fun wtf(String, Throwable? = null, Map<String, Any?> = emptyMap())
     */
    @Test
    fun logs_logger_wtf_log_with_error() {
        val testMethodName = "logs_logger_wtf_log_with_error"
        val fakeThrowable = forge.aThrowable()
        val fakeMessage = forge.anAlphaNumericalString()
        val attributes = defaultTestAttributes(testMethodName)
        measure(testMethodName) {
            logger.wtf(
                fakeMessage,
                fakeThrowable,
                attributes
            )
        }
    }

    /**
     * apiMethodSignature: Logger#fun addAttribute(String, Boolean)
     */
    @Test
    fun logs_logger_add_boolean_attribute() {
        val testMethodName = "logs_logger_add_boolean_attribute"
        measure(testMethodName) {
            logger.addAttribute(SPECIAL_BOOL_ATTRIBUTE_NAME, forge.aBool())
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun addAttribute(String, Int)
     */
    @Test
    fun logs_logger_add_int_attribute() {
        val testMethodName = "logs_logger_add_int_attribute"
        measure(testMethodName) {
            logger.addAttribute(SPECIAL_INT_ATTRIBUTE_NAME, forge.anInt(min = 11))
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun addAttribute(String, String?)
     */
    @Test
    fun logs_logger_add_string_attribute() {
        val testMethodName = "logs_logger_add_string_attribute"
        val fakeString = forge.aStringMatching("customAttribute[a-z0-9_:./-]{1,20}")
        measure(testMethodName) {
            logger.addAttribute(SPECIAL_STRING_ATTRIBUTE_NAME, fakeString)
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun addAttribute(String, String?)
     */
    @Test
    fun logs_logger_add_string_null_attribute() {
        val testMethodName = "logs_logger_add_string_null_attribute"
        val nullString: String? = null
        measure(testMethodName) {
            logger.addAttribute(SPECIAL_STRING_ATTRIBUTE_NAME, nullString)
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun addAttribute(String, Float)
     */
    @Test
    fun logs_logger_add_float_attribute() {
        val testMethodName = "logs_logger_add_float_attribute"
        measure(testMethodName) {
            logger.addAttribute(SPECIAL_FLOAT_ATTRIBUTE_NAME, forge.aFloat(min = 11f))
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun addAttribute(String, Double)
     */
    @Test
    fun logs_logger_add_double_attribute() {
        val testMethodName = "logs_logger_add_double_attribute"
        measure(testMethodName) {
            logger.addAttribute(SPECIAL_DOUBLE_ATTRIBUTE_NAME, forge.aDouble(min = 11.0))
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun addAttribute(String, JsonArray?)
     */
    @Test
    fun logs_logger_add_jsonarray_attribute() {
        val testMethodName = "logs_logger_add_jsonarray_attribute"
        measure(testMethodName) {
            logger.addAttribute(SPECIAL_JSONARRAY_ATTRIBUTE_NAME, CUSTOM_JSON_ARRAY_ATTRIBUTE)
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun addAttribute(String, JsonArray?)
     */
    @Test
    fun logs_logger_add_jsonarray_null_attribute() {
        val testMethodName = "logs_logger_add_jsonarray_null_attribute"
        val nullJsonArray: JsonArray? = null
        measure(testMethodName) {
            logger.addAttribute(SPECIAL_JSONARRAY_ATTRIBUTE_NAME, nullJsonArray)
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun addAttribute(String, JsonObject?)
     */
    @Test
    fun logs_logger_add_jsonobject_attribute() {
        val testMethodName = "logs_logger_add_jsonobject_attribute"
        measure(testMethodName) {
            logger.addAttribute(SPECIAL_JSONOBJECT_ATTRIBUTE_NAME, CUSTOM_JSON_OBJECT_ATTRIBUTE)
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun addAttribute(String, JsonObject?)
     */
    @Test
    fun logs_logger_add_jsonobject_null_attribute() {
        val testMethodName = "logs_logger_add_jsonobject_null_attribute"
        val nullJsonObject: JsonObject? = null
        measure(testMethodName) {
            logger.addAttribute(SPECIAL_JSONOBJECT_ATTRIBUTE_NAME, nullJsonObject)
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun addDate(String, Date?)
     */
    @Test
    fun logs_logger_add_date_attribute() {
        val testMethodName = "logs_logger_add_date_attribute"
        measure(testMethodName) {
            logger.addAttribute(SPECIAL_DATE_ATTRIBUTE_NAME, CUSTOM_DATE_ATTRIBUTE)
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun addDate(String, Date?)
     */
    @Test
    fun logs_logger_add_date_null_attribute() {
        val testMethodName = "logs_logger_add_date_null_attribute"
        val nullDate: Date? = null
        measure(testMethodName) {
            logger.addAttribute(SPECIAL_DATE_ATTRIBUTE_NAME, nullDate)
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun removeAttribute(String)
     */
    @Test
    fun logs_logger_remove_attribute() {
        val testMethodName = "logs_logger_remove_attribute"
        when (forge.anInt(min = 1, max = 9)) {
            1 -> logger.addAttribute(
                SPECIAL_ATTRIBUTE_NAME,
                forge.aNullable { forge.anAlphabeticalString() }
            )
            2 -> logger.addAttribute(SPECIAL_ATTRIBUTE_NAME, forge.anInt())
            3 -> logger.addAttribute(SPECIAL_ATTRIBUTE_NAME, forge.aFloat())
            4 -> logger.addAttribute(SPECIAL_ATTRIBUTE_NAME, forge.aDouble())
            5 -> logger.addAttribute(SPECIAL_ATTRIBUTE_NAME, forge.aBool())
            6 -> logger.addAttribute(
                SPECIAL_ATTRIBUTE_NAME,
                forge.aNullable { CUSTOM_JSON_OBJECT_ATTRIBUTE }
            )
            7 -> logger.addAttribute(
                SPECIAL_ATTRIBUTE_NAME,
                forge.aNullable { CUSTOM_JSON_ARRAY_ATTRIBUTE }
            )
            8 -> logger.addAttribute(
                SPECIAL_ATTRIBUTE_NAME,
                forge.aNullable { CUSTOM_DATE_ATTRIBUTE }
            )
        }
        measure(testMethodName) {
            logger.removeAttribute(SPECIAL_ATTRIBUTE_NAME)
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun addTag(String, String)
     */
    @Test
    fun logs_logger_add_tag() {
        val testMethodName = "logs_logger_add_tag"
        measure(testMethodName) {
            logger.addTag(SPECIAL_TAG_NAME, forge.aStringMatching("customTag[a-z0-9_:./-]{1,20}"))
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun addTag(String, String)
     */
    @Test
    fun logs_logger_add_already_formatted_tag() {
        val testMethodName = "logs_logger_add_already_formatted_tag"
        measure(testMethodName) {
            logger.addTag("$SPECIAL_TAG_NAME:$CUSTOM_STRING_TAG")
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    /**
     * apiMethodSignature: Logger#fun removeTag(String)
     */
    @Test
    fun logs_logger_remove_tag() {
        val testMethodName = "logs_logger_remove_tag"
        if (forge.aBool()) {
            logger.addTag(SPECIAL_TAG_NAME, CUSTOM_STRING_TAG)
        } else {
            logger.addTag("$SPECIAL_TAG_NAME:$CUSTOM_STRING_TAG")
        }
        measure(testMethodName) {
            logger.removeTag("$SPECIAL_TAG_NAME:$CUSTOM_STRING_TAG")
        }
        logger.sendRandomLog(testMethodName, forge)
    }

    companion object {
        const val CUSTOM_STRING_TAG: String = "custom_tag"
        const val CUSTOM_INT_ATTRIBUTE: Int = 1
        private const val CUSTOM_JSON_OBJECT_PROPERTY = "custom_json_property"
        val CUSTOM_JSON_ARRAY_ATTRIBUTE: JsonArray = JsonArray().apply {
            this.add(
                CUSTOM_INT_ATTRIBUTE
            )
        }
        val CUSTOM_JSON_OBJECT_ATTRIBUTE: JsonObject = JsonObject().apply {
            addProperty(CUSTOM_JSON_OBJECT_PROPERTY, CUSTOM_INT_ATTRIBUTE)
        }
        val CUSTOM_DATE_ATTRIBUTE: Date = SimpleDateFormat("yyyy-MM-dd").parse("2021-01-01")!!
    }
}
