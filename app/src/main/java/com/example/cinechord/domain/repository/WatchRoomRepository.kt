package com.example.cinechord.domain.repository

import com.example.cinechord.domain.models.Message
import com.example.cinechord.domain.models.WatchRoom
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for watch room related operations.
 */
interface WatchRoomRepository {
    suspend fun createRoom(room: WatchRoom): Result<String>
    suspend fun joinRoom(roomId: String, userId: String): Result<Unit>
    suspend fun leaveRoom(roomId: String, userId: String): Result<Unit>
    suspend fun deleteRoom(roomId: String): Result<Unit>
    suspend fun updateRoomPlaybackState(roomId: String, timestamp: Long, isPlaying: Boolean): Result<Unit>
    suspend fun updateRoomVideoUrl(roomId: String, videoUrl: String): Result<Unit>
    fun getRooms(): Flow<List<WatchRoom>>
    fun getRoomById(roomId: String): Flow<WatchRoom?>
    fun getUserRooms(userId: String): Flow<List<WatchRoom>>
    
    /**
     * Kullanıcının oluşturduğu odaları getirir.
     */
    fun getUserCreatedRooms(userId: String): Flow<List<WatchRoom>>
    
    // Chat methods
    suspend fun sendMessage(message: Message): Result<String>
    fun getRoomMessages(roomId: String): Flow<List<Message>>
} 