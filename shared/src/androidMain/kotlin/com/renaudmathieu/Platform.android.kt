package com.renaudmathieu

import android.os.Build
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual val platformModule = module {
    singleOf(::DefaultAudioPlayer) bind AudioPlayer::class
}