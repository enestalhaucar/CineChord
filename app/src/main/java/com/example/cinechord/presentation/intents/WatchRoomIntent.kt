package com.example.cinechord.presentation.intents

/**
 * Intents for watch room-related user actions in MVI pattern.
 */
sealed class WatchRoomIntent {
    data class CreateRoom(
        val name: String,
        val description: String,
        val ownerId: String,
        val videoUrl: String
    ) : WatchRoomIntent()
    
    data class JoinRoom(val roomId: String, val userId: String) : WatchRoomIntent()
    data class LeaveRoom(val roomId: String, val userId: String) : WatchRoomIntent()
    data class DeleteRoom(val roomId: String) : WatchRoomIntent()
    
    data class UpdatePlaybackState(
        val roomId: String,
        val timestamp: Long,
        val isPlaying: Boolean
    ) : WatchRoomIntent()
    
    data class UpdateVideoUrl(val roomId: String, val videoUrl: String) : WatchRoomIntent()
    object GetRooms : WatchRoomIntent()
    data class GetRoomById(val roomId: String) : WatchRoomIntent()
    data class GetUserRooms(val userId: String) : WatchRoomIntent()
    data class GetUserCreatedRooms(val userId: String) : WatchRoomIntent()
    
    // Chat related intents
    data class SendMessage(
        val roomId: String,
        val senderId: String,
        val senderName: String,
        val senderProfileImage: String,
        val content: String,
        val type: String
    ) : WatchRoomIntent()
    
    data class GetMessages(val roomId: String) : WatchRoomIntent()
    
    // Reset states
    object ResetVideoUrlUpdatedState : WatchRoomIntent()
    object ResetMessageSentState : WatchRoomIntent()
    object ResetRoomCreatedState : WatchRoomIntent()
} 