package com.example.cinechord.data.repository

import com.example.cinechord.data.models.MessageDto
import com.example.cinechord.data.models.UserDto
import com.example.cinechord.data.models.WatchRoomDto
import com.example.cinechord.domain.models.Message
import com.example.cinechord.domain.models.WatchRoom
import com.example.cinechord.domain.repository.WatchRoomRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class WatchRoomRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : WatchRoomRepository {

    companion object {
        private const val ROOMS_REF = "rooms"
        private const val MESSAGES_REF = "messages"
        private const val USERS_REF = "users"
    }

    override suspend fun createRoom(room: WatchRoom): Result<String> {
        return try {
            val roomsRef = database.reference.child(ROOMS_REF)
            val newRoomRef = roomsRef.push()
            val roomId = newRoomRef.key ?: return Result.failure(Exception("Failed to create room ID"))
            
            val roomDto = WatchRoomDto.fromDomain(room.copy(id = roomId))
            newRoomRef.setValue(roomDto).await()
            
            // Add room ID to owner's joined rooms
            val userRef = database.reference.child(USERS_REF).child(room.ownerId)
            val userSnapshot = userRef.get().await()
            val userDto = userSnapshot.getValue(UserDto::class.java)
            
            if (userDto != null) {
                val updatedRoomIds = userDto.joinedRoomIds.toMutableList()
                if (!updatedRoomIds.contains(roomId)) {
                    updatedRoomIds.add(roomId)
                    userRef.child("joinedRoomIds").setValue(updatedRoomIds).await()
                }
            }
            
            Result.success(roomId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinRoom(roomId: String, userId: String): Result<Unit> {
        return try {
            // Add user to room participants
            val roomRef = database.reference.child(ROOMS_REF).child(roomId)
            val roomSnapshot = roomRef.get().await()
            val roomDto = roomSnapshot.getValue(WatchRoomDto::class.java)
                ?: return Result.failure(Exception("Room not found"))
                
            val participants = roomDto.participants.toMutableList()
            if (!participants.contains(userId)) {
                participants.add(userId)
                roomRef.child("participants").setValue(participants).await()
            }
            
            // Add room to user's joined rooms
            val userRef = database.reference.child(USERS_REF).child(userId)
            val userSnapshot = userRef.get().await()
            val userDto = userSnapshot.getValue(UserDto::class.java)
            
            if (userDto != null) {
                val joinedRooms = userDto.joinedRoomIds.toMutableList()
                if (!joinedRooms.contains(roomId)) {
                    joinedRooms.add(roomId)
                    userRef.child("joinedRoomIds").setValue(joinedRooms).await()
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveRoom(roomId: String, userId: String): Result<Unit> {
        return try {
            // Remove user from room participants
            val roomRef = database.reference.child(ROOMS_REF).child(roomId)
            val roomSnapshot = roomRef.get().await()
            val roomDto = roomSnapshot.getValue(WatchRoomDto::class.java)
                ?: return Result.failure(Exception("Room not found"))
                
            val participants = roomDto.participants.toMutableList()
            if (participants.contains(userId)) {
                participants.remove(userId)
                roomRef.child("participants").setValue(participants).await()
            }
            
            // Remove room from user's joined rooms
            val userRef = database.reference.child(USERS_REF).child(userId)
            val userSnapshot = userRef.get().await()
            val userDto = userSnapshot.getValue(UserDto::class.java)
            
            if (userDto != null) {
                val joinedRooms = userDto.joinedRoomIds.toMutableList()
                if (joinedRooms.contains(roomId)) {
                    joinedRooms.remove(roomId)
                    userRef.child("joinedRoomIds").setValue(joinedRooms).await()
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRoom(roomId: String): Result<Unit> {
        return try {
            // Get room to find all participants
            val roomRef = database.reference.child(ROOMS_REF).child(roomId)
            val roomSnapshot = roomRef.get().await()
            val roomDto = roomSnapshot.getValue(WatchRoomDto::class.java)
            
            // Remove room ID from all participants' joined rooms
            if (roomDto != null) {
                for (userId in roomDto.participants) {
                    val userRef = database.reference.child(USERS_REF).child(userId)
                    val userSnapshot = userRef.get().await()
                    val userDto = userSnapshot.getValue(UserDto::class.java)
                    
                    if (userDto != null) {
                        val joinedRooms = userDto.joinedRoomIds.toMutableList()
                        if (joinedRooms.contains(roomId)) {
                            joinedRooms.remove(roomId)
                            userRef.child("joinedRoomIds").setValue(joinedRooms).await()
                        }
                    }
                }
            }
            
            // Delete all messages in the room
            val messagesRef = database.reference.child(MESSAGES_REF).child(roomId)
            messagesRef.removeValue().await()
            
            // Delete the room
            roomRef.removeValue().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRoomPlaybackState(
        roomId: String, 
        timestamp: Long, 
        isPlaying: Boolean
    ): Result<Unit> {
        return try {
            val roomRef = database.reference.child(ROOMS_REF).child(roomId)
            roomRef.child("currentTimestamp").setValue(timestamp).await()
            roomRef.child("isPlaying").setValue(isPlaying).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRoomVideoUrl(roomId: String, videoUrl: String): Result<Unit> {
        return try {
            val roomRef = database.reference.child(ROOMS_REF).child(roomId)
            roomRef.child("videoUrl").setValue(videoUrl).await()
            roomRef.child("currentTimestamp").setValue(0).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRooms(): Flow<List<WatchRoom>> = callbackFlow {
        val roomsRef = database.reference.child(ROOMS_REF)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = mutableListOf<WatchRoom>()
                for (roomSnapshot in snapshot.children) {
                    val roomDto = roomSnapshot.getValue(WatchRoomDto::class.java)
                    if (roomDto != null) {
                        rooms.add(roomDto.toDomain())
                    }
                }
                trySend(rooms)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        
        roomsRef.addValueEventListener(listener)
        
        awaitClose {
            roomsRef.removeEventListener(listener)
        }
    }

    override fun getRoomById(roomId: String): Flow<WatchRoom?> = callbackFlow {
        val roomRef = database.reference.child(ROOMS_REF).child(roomId)
        Log.d("WatchRoomRepo", "Fetching room with ID: $roomId")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("WatchRoomRepo", "Data changed for room $roomId. Exists: ${snapshot.exists()}")
                
                val roomDto = snapshot.getValue(WatchRoomDto::class.java)
                Log.d("WatchRoomRepo", "Room DTO: $roomDto")
                
                if (roomDto != null) {
                    Log.d("WatchRoomRepo", "Room video URL from DTO: '${roomDto.videoUrl}'")
                    val domain = roomDto.toDomain()
                    Log.d("WatchRoomRepo", "Room video URL from domain: '${domain.videoUrl}'")
                    trySend(domain)
                } else {
                    Log.e("WatchRoomRepo", "Failed to parse room data for $roomId")
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WatchRoomRepo", "Error fetching room $roomId: ${error.message}")
            }
        }
        
        roomRef.addValueEventListener(listener)
        
        awaitClose {
            roomRef.removeEventListener(listener)
        }
    }

    override fun getUserRooms(userId: String): Flow<List<WatchRoom>> = callbackFlow {
        val userRef = database.reference.child(USERS_REF).child(userId)
        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userDto = snapshot.getValue(UserDto::class.java)
                if (userDto != null && userDto.joinedRoomIds.isNotEmpty()) {
                    val joinedRoomIds = userDto.joinedRoomIds
                    
                    // Now get all the rooms
                    val roomsRef = database.reference.child(ROOMS_REF)
                    roomsRef.get().addOnSuccessListener { roomsSnapshot ->
                        val rooms = mutableListOf<WatchRoom>()
                        for (roomSnapshot in roomsSnapshot.children) {
                            val roomDto = roomSnapshot.getValue(WatchRoomDto::class.java)
                            if (roomDto != null && joinedRoomIds.contains(roomDto.id)) {
                                rooms.add(roomDto.toDomain())
                            }
                        }
                        trySend(rooms)
                    }
                } else {
                    trySend(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        
        userRef.addValueEventListener(userListener)
        
        awaitClose {
            userRef.removeEventListener(userListener)
        }
    }

    override fun getUserCreatedRooms(userId: String): Flow<List<WatchRoom>> = callbackFlow {
        val roomsRef = database.reference.child(ROOMS_REF)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = mutableListOf<WatchRoom>()
                for (roomSnapshot in snapshot.children) {
                    val roomDto = roomSnapshot.getValue(WatchRoomDto::class.java)
                    if (roomDto != null && roomDto.ownerId == userId) {
                        rooms.add(roomDto.toDomain())
                    }
                }
                trySend(rooms)
            }
            
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        
        roomsRef.addValueEventListener(listener)
        
        awaitClose {
            roomsRef.removeEventListener(listener)
        }
    }

    override suspend fun sendMessage(message: Message): Result<String> {
        return try {
            val messagesRef = database.reference.child(MESSAGES_REF)
                .child(message.roomId)
            
            val newMessageRef = messagesRef.push()
            val messageId = newMessageRef.key ?: return Result.failure(Exception("Failed to create message ID"))
            
            val messageDto = MessageDto.fromDomain(message.copy(id = messageId))
            newMessageRef.setValue(messageDto).await()
            
            Result.success(messageId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRoomMessages(roomId: String): Flow<List<Message>> = callbackFlow {
        val messagesRef = database.reference.child(MESSAGES_REF).child(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val messageDto = messageSnapshot.getValue(MessageDto::class.java)
                    if (messageDto != null) {
                        messages.add(messageDto.toDomain())
                    }
                }
                trySend(messages.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        
        messagesRef.addValueEventListener(listener)
        
        awaitClose {
            messagesRef.removeEventListener(listener)
        }
    }
} 