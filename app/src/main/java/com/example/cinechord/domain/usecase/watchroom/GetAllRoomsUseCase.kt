package com.example.cinechord.domain.usecase.watchroom

import com.example.cinechord.domain.models.WatchRoom
import com.example.cinechord.domain.repository.WatchRoomRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllRoomsUseCase @Inject constructor(
    private val watchRoomRepository: WatchRoomRepository
) {
    operator fun invoke(): Flow<List<WatchRoom>> {
        return watchRoomRepository.getRooms()
    }
} 