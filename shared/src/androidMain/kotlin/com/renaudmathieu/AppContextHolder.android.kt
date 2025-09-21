package com.renaudmathieu

import android.content.Context

object AppContextHolder {
    @Volatile
    private var appCtx: Context? = null

    val applicationContext: Context
        get() = appCtx ?: throw IllegalStateException("AppContextHolder not initialized. Call AppContextHolder.init(context) early in Application/Activity.")

    fun init(context: Context) {
        appCtx = context.applicationContext
    }
}