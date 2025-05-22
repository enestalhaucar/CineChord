package com.example.cinechord.domain.models

/**
 * Domain model for a chat message in a watch room.
 */
data class Message(
    val id: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderProfileImage: String = "",
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageType {
    TEXT,
    EMOJI,
    SYSTEM
} 