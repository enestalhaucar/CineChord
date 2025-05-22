package com.example.cinechord.domain.models

/**
 * Domain model for a watch room.
 */
data class WatchRoom(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val videoUrl: String = "",
    val currentTimestamp: Long = 0,
    val isPlaying: Boolean = false,
    val participants: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) 