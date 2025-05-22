package com.example.cinechord.domain.usecase.watchroom

import com.example.cinechord.domain.models.WatchRoom
import com.example.cinechord.domain.repository.WatchRoomRepository
import javax.inject.Inject

class CreateWatchRoomUseCase @Inject constructor(
    private val watchRoomRepository: WatchRoomRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        ownerId: String,
        videoUrl: String
    ): Result<String> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Room name cannot be empty"))
        }
        
        if (ownerId.isBlank()) {
            return Result.failure(IllegalArgumentException("Owner ID cannot be empty"))
        }
        
        val room = WatchRoom(
            name = name,
            description = description,
            ownerId = ownerId,
            videoUrl = videoUrl,
            participants = listOf(ownerId),
            currentTimestamp = 0L,
            isPlaying = false
        )
        
        return watchRoomRepository.createRoom(room)
    }
} 