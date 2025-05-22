package com.example.cinechord.presentation.states

import com.example.cinechord.domain.models.User

/**
 * State for authentication in MVI pattern.
 */
data class AuthState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val isSignInSuccessful: Boolean = false,
    val isSignUpSuccessful: Boolean = false,
    val isSignOutSuccessful: Boolean = false
) 