package com.example.cinechord.data.models

import com.example.cinechord.domain.models.User

/**
 * Data transfer object for User that maps to Firebase.
 */
data class UserDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val joinedRoomIds: List<String> = emptyList()
) {
    fun toDomain(): User {
        return User(
            id = id,
            name = name,
            email = email,
            profileImageUrl = profileImageUrl,
            joinedRoomIds = joinedRoomIds
        )
    }
    
    companion object {
        fun fromDomain(user: User): UserDto {
            return UserDto(
                id = user.id,
                name = user.name,
                email = user.email,
                profileImageUrl = user.profileImageUrl,
                joinedRoomIds = user.joinedRoomIds
            )
        }
    }
} 