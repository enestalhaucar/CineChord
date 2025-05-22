package com.example.cinechord.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinechord.domain.models.Message
import com.example.cinechord.domain.models.MessageType
import com.example.cinechord.domain.repository.WatchRoomRepository
import com.example.cinechord.domain.usecase.watchroom.CreateWatchRoomUseCase
import com.example.cinechord.domain.usecase.watchroom.GetWatchRoomsUseCase
import com.example.cinechord.presentation.intents.WatchRoomIntent
import com.example.cinechord.presentation.states.WatchRoomState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchRoomViewModel @Inject constructor(
    private val createWatchRoomUseCase: CreateWatchRoomUseCase,
    private val getWatchRoomsUseCase: GetWatchRoomsUseCase,
    private val watchRoomRepository: WatchRoomRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(WatchRoomState())
    val state: StateFlow<WatchRoomState> = _state.asStateFlow()
    
    fun processIntent(intent: WatchRoomIntent) {
        when (intent) {
            is WatchRoomIntent.CreateRoom -> createRoom(
                intent.name,
                intent.description,
                intent.ownerId,
                intent.videoUrl
            )
            is WatchRoomIntent.JoinRoom -> joinRoom(intent.roomId, intent.userId)
            is WatchRoomIntent.LeaveRoom -> leaveRoom(intent.roomId, intent.userId)
            is WatchRoomIntent.DeleteRoom -> deleteRoom(intent.roomId)
            is WatchRoomIntent.UpdatePlaybackState -> updatePlaybackState(
                intent.roomId,
                intent.timestamp,
                intent.isPlaying
            )
            is WatchRoomIntent.UpdateVideoUrl -> updateVideoUrl(intent.roomId, intent.videoUrl)
            is WatchRoomIntent.GetRooms -> getRooms()
            is WatchRoomIntent.GetRoomById -> getRoomById(intent.roomId)
            is WatchRoomIntent.GetUserRooms -> getUserRooms(intent.userId)
            is WatchRoomIntent.GetUserCreatedRooms -> getUserCreatedRooms(intent.userId)
            is WatchRoomIntent.SendMessage -> sendMessage(
                intent.roomId,
                intent.senderId,
                intent.senderName,
                intent.senderProfileImage,
                intent.content,
                intent.type
            )
            is WatchRoomIntent.GetMessages -> getMessages(intent.roomId)
            is WatchRoomIntent.ResetVideoUrlUpdatedState -> resetVideoUrlUpdatedState()
            is WatchRoomIntent.ResetMessageSentState -> resetMessageSentState()
            is WatchRoomIntent.ResetRoomCreatedState -> resetRoomCreatedState()
        }
    }
    
    private fun createRoom(name: String, description: String, ownerId: String, videoUrl: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isRoomCreated = false,
                    createdRoomId = null
                )
            }
            
            val result = createWatchRoomUseCase(name, description, ownerId, videoUrl)
            
            result.fold(
                onSuccess = { roomId ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRoomCreated = true,
                            createdRoomId = roomId
                        )
                    }
                    // Get the room to add it to the current state
                    getRoomById(roomId)
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "An unknown error occurred",
                            isRoomCreated = false
                        )
                    }
                }
            )
        }
    }
    
    private fun joinRoom(roomId: String, userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isRoomJoined = false) }
            
            val result = watchRoomRepository.joinRoom(roomId, userId)
            
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRoomJoined = true
                        )
                    }
                    // Get the updated room
                    getRoomById(roomId)
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "An unknown error occurred",
                            isRoomJoined = false
                        )
                    }
                }
            )
        }
    }
    
    private fun leaveRoom(roomId: String, userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isRoomLeft = false) }
            
            val result = watchRoomRepository.leaveRoom(roomId, userId)
            
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRoomLeft = true,
                            currentRoom = if (it.currentRoom?.id == roomId) null else it.currentRoom
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "An unknown error occurred",
                            isRoomLeft = false
                        )
                    }
                }
            )
        }
    }
    
    private fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isRoomDeleted = false) }
            
            val result = watchRoomRepository.deleteRoom(roomId)
            
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRoomDeleted = true,
                            rooms = it.rooms.filter { room -> room.id != roomId },
                            currentRoom = if (it.currentRoom?.id == roomId) null else it.currentRoom
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "An unknown error occurred",
                            isRoomDeleted = false
                        )
                    }
                }
            )
        }
    }
    
    private fun updatePlaybackState(roomId: String, timestamp: Long, isPlaying: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isPlaybackUpdated = false) }
            
            val result = watchRoomRepository.updateRoomPlaybackState(roomId, timestamp, isPlaying)
            
            result.fold(
                onSuccess = {
                    _state.update {
                        val updatedRoom = it.currentRoom?.copy(
                            currentTimestamp = timestamp,
                            isPlaying = isPlaying
                        )
                        
                        it.copy(
                            isLoading = false,
                            isPlaybackUpdated = true,
                            currentRoom = updatedRoom
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "An unknown error occurred",
                            isPlaybackUpdated = false
                        )
                    }
                }
            )
        }
    }
    
    private fun updateVideoUrl(roomId: String, videoUrl: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isVideoUrlUpdated = false) }
            
            val result = watchRoomRepository.updateRoomVideoUrl(roomId, videoUrl)
            
            result.fold(
                onSuccess = {
                    _state.update {
                        val updatedRoom = it.currentRoom?.copy(
                            videoUrl = videoUrl,
                            currentTimestamp = 0
                        )
                        
                        it.copy(
                            isLoading = false,
                            isVideoUrlUpdated = true,
                            currentRoom = updatedRoom
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "An unknown error occurred",
                            isVideoUrlUpdated = false
                        )
                    }
                }
            )
        }
    }
    
    private fun getRooms() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            getWatchRoomsUseCase().collect { rooms ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        rooms = rooms
                    )
                }
            }
        }
    }
    
    private fun getRoomById(roomId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            watchRoomRepository.getRoomById(roomId).collect { room ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        currentRoom = room,
                        rooms = if (room != null) {
                            val updatedRooms = it.rooms.toMutableList()
                            val index = updatedRooms.indexOfFirst { r -> r.id == room.id }
                            if (index >= 0) {
                                updatedRooms[index] = room
                            } else {
                                updatedRooms.add(room)
                            }
                            updatedRooms
                        } else {
                            it.rooms
                        }
                    )
                }
            }
        }
    }
    
    private fun getUserRooms(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            watchRoomRepository.getUserRooms(userId).collect { rooms ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        rooms = rooms
                    )
                }
            }
        }
    }
    
    private fun getUserCreatedRooms(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            watchRoomRepository.getUserCreatedRooms(userId).collect { rooms ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        rooms = rooms
                    )
                }
            }
        }
    }
    
    private fun sendMessage(
        roomId: String,
        senderId: String,
        senderName: String,
        senderProfileImage: String,
        content: String,
        type: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isMessageLoading = true, error = null, isMessageSent = false) }
            
            val messageType = try {
                MessageType.valueOf(type)
            } catch (e: Exception) {
                MessageType.TEXT
            }
            
            val message = Message(
                roomId = roomId,
                senderId = senderId,
                senderName = senderName,
                senderProfileImage = senderProfileImage,
                content = content,
                type = messageType
            )
            
            val result = watchRoomRepository.sendMessage(message)
            
            result.fold(
                onSuccess = { messageId ->
                    _state.update {
                        it.copy(
                            isMessageLoading = false,
                            isMessageSent = true,
                            sentMessageId = messageId
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isMessageLoading = false,
                            error = error.message ?: "An unknown error occurred",
                            isMessageSent = false
                        )
                    }
                }
            )
        }
    }
    
    private fun getMessages(roomId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            watchRoomRepository.getRoomMessages(roomId).collect { messages ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        messages = messages
                    )
                }
            }
        }
    }
    
    fun resetState() {
        _state.update {
            it.copy(
                error = null,
                isRoomCreated = false,
                isRoomJoined = false,
                isRoomLeft = false,
                isRoomDeleted = false,
                isPlaybackUpdated = false,
                isVideoUrlUpdated = false,
                isMessageSent = false,
                createdRoomId = null,
                sentMessageId = null
            )
        }
    }

    // Sadece mesaj ile ilgili state'leri sıfırla
    fun resetMessageState() {
        _state.update {
            it.copy(
                isMessageSent = false,
                isMessageLoading = false,
                sentMessageId = null
            )
        }
    }

    // Resets the video URL updated state flag
    private fun resetVideoUrlUpdatedState() {
        _state.update {
            it.copy(isVideoUrlUpdated = false)
        }
    }

    // Only resets the isMessageSent flag
    private fun resetMessageSentState() {
        _state.update {
            it.copy(isMessageSent = false)
        }
    }

    private fun resetRoomCreatedState() {
        _state.update {
            it.copy(isRoomCreated = false, createdRoomId = null)
        }
    }
} 