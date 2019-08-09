package com.ineat.firebase.natural.language

import java.util.concurrent.TimeUnit

data class Message(val isMe: Boolean = true, val text: String, val createAt: Long)

val MESSAGES = mutableListOf(
    Message(
        isMe = true,
        text = "Quelle est la température du salon ?",
        createAt = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(4)
    ),
    Message(
        isMe = false,
        text = "Il fait actuellement 18°c dans le salon",
        createAt = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(4)
    ),
    Message(
        isMe = true,
        text = "La porte d'entrée est-elle fermée ?",
        createAt = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(4)
    ),
    Message(
        isMe = false,
        text = "La porte est actuellement fermée",
        createAt = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(4)
    )
)