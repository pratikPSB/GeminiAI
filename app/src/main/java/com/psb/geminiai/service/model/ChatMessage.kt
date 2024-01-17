package com.google.ai.sample.feature.chat

import android.graphics.Bitmap
import android.text.format.DateUtils
import java.util.Date
import java.util.UUID

enum class Participant {
    USER, MODEL, ERROR
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    var img: Bitmap? = null,
    var text: String = "",
    val participant: Participant = Participant.USER,
    var time: Long = System.currentTimeMillis(),
    var isPending: Boolean = false
)
