package com.example.cinechord.domain.models

/**
 * Domain model for a user.
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val joinedRoomIds: List<String> = emptyList()
) 