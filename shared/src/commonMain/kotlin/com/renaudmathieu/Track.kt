package com.renaudmathieu

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val url: String,
    val duration: Long
)