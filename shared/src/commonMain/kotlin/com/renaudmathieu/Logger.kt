package com.renaudmathieu

import co.touchlab.kermit.Logger as KermitLogger

/**
 * A centralized logger for the application using Kermit.
 * This class provides logging functionality across all platforms.
 */
object Logger {
    // Create the base logger with a default tag
    private val baseLogger = KermitLogger.withTag("GrooveBox")

    // Get a logger with a specific tag
    fun withTag(tag: String): KermitLogger {
        return baseLogger.withTag(tag)
    }

    // Convenience methods for logging with the base logger
    fun v(message: String, throwable: Throwable? = null, tag: String? = null) {
        val logger = tag?.let { withTag(it) } ?: baseLogger
        logger.v(message, throwable)
    }

    fun d(message: String, throwable: Throwable? = null, tag: String? = null) {
        val logger = tag?.let { withTag(it) } ?: baseLogger
        logger.d(message, throwable)
    }

    fun i(message: String, throwable: Throwable? = null, tag: String? = null) {
        val logger = tag?.let { withTag(it) } ?: baseLogger
        logger.i(message, throwable)
    }

    fun w(message: String, throwable: Throwable? = null, tag: String? = null) {
        val logger = tag?.let { withTag(it) } ?: baseLogger
        logger.w(message, throwable)
    }

    fun e(message: String, throwable: Throwable? = null, tag: String? = null) {
        val logger = tag?.let { withTag(it) } ?: baseLogger
        logger.e(message, throwable)
    }

    fun a(message: String, throwable: Throwable? = null, tag: String? = null) {
        val logger = tag?.let { withTag(it) } ?: baseLogger
        logger.a(message, throwable)
    }
}
