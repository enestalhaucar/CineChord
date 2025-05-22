package com.example.cinechord.presentation.intents

/**
 * Intents for authentication-related user actions in MVI pattern.
 */
sealed class AuthIntent {
    data class SignIn(val email: String, val password: String) : AuthIntent()
    data class SignUp(val name: String, val email: String, val password: String) : AuthIntent()
    object SignOut : AuthIntent()
    object GetCurrentUser : AuthIntent()
} 