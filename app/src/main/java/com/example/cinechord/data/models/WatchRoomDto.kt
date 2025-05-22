package com.example.cinechord.data.models

import com.example.cinechord.domain.models.WatchRoom

/**
 * Data transfer object for WatchRoom that maps to Firebase.
 */
data class WatchRoomDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val videoUrl: String = "",
    val currentTimestamp: Long = 0,
    val isPlaying: Boolean = false,
    val participants: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): WatchRoom {
        return WatchRoom(
            id = id,
            name = name,
            description = description,
            ownerId = ownerId,
            videoUrl = videoUrl,
            currentTimestamp = currentTimestamp,
            isPlaying = isPlaying,
            participants = participants,
            createdAt = createdAt
        )
    }
    
    companion object {
        fun fromDomain(watchRoom: WatchRoom): WatchRoomDto {
            return WatchRoomDto(
                id = watchRoom.id,
                name = watchRoom.name,
                description = watchRoom.description,
                ownerId = watchRoom.ownerId,
                videoUrl = watchRoom.videoUrl,
                currentTimestamp = watchRoom.currentTimestamp,
                isPlaying = watchRoom.isPlaying,
                participants = watchRoom.participants,
                createdAt = watchRoom.createdAt
            )
        }
    }
} 