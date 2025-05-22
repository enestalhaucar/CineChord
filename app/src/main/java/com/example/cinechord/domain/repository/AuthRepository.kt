package com.example.cinechord.domain.repository

import com.example.cinechord.domain.models.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication related operations.
 */
interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(name: String, email: String, password: String): Result<User>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): Flow<User?>
    fun isUserAuthenticated(): Boolean
} 