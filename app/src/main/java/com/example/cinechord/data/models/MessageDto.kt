package com.example.cinechord.data.models

import com.example.cinechord.domain.models.Message
import com.example.cinechord.domain.models.MessageType

/**
 * Data transfer object for Message that maps to Firebase.
 */
data class MessageDto(
    val id: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderProfileImage: String = "",
    val content: String = "",
    val type: String = MessageType.TEXT.name,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): Message {
        return Message(
            id = id,
            roomId = roomId,
            senderId = senderId,
            senderName = senderName,
            senderProfileImage = senderProfileImage,
            content = content,
            type = try {
                MessageType.valueOf(type)
            } catch (e: Exception) {
                MessageType.TEXT
            },
            timestamp = timestamp
        )
    }
    
    companion object {
        fun fromDomain(message: Message): MessageDto {
            return MessageDto(
                id = message.id,
                roomId = message.roomId,
                senderId = message.senderId,
                senderName = message.senderName,
                senderProfileImage = message.senderProfileImage,
                content = message.content,
                type = message.type.name,
                timestamp = message.timestamp
            )
        }
    }
} 