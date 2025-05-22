package com.example.cinechord.presentation.states

import com.example.cinechord.domain.models.Message
import com.example.cinechord.domain.models.WatchRoom

/**
 * State for watch rooms in MVI pattern.
 */
data class WatchRoomState(
    val isLoading: Boolean = false,
    val isMessageLoading: Boolean = false,
    val rooms: List<WatchRoom> = emptyList(),
    val currentRoom: WatchRoom? = null,
    val messages: List<Message> = emptyList(),
    val error: String? = null,
    
    // Operation states
    val isRoomCreated: Boolean = false,
    val isRoomJoined: Boolean = false,
    val isRoomLeft: Boolean = false,
    val isRoomDeleted: Boolean = false,
    val isPlaybackUpdated: Boolean = false,
    val isVideoUrlUpdated: Boolean = false,
    val isMessageSent: Boolean = false,
    
    // IDs returned from operations
    val createdRoomId: String? = null,
    val sentMessageId: String? = null
) 