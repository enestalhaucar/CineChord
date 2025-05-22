package com.example.cinechord.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinechord.domain.usecase.auth.SignInUseCase
import com.example.cinechord.domain.usecase.auth.SignUpUseCase
import com.example.cinechord.domain.repository.AuthRepository
import com.example.cinechord.presentation.intents.AuthIntent
import com.example.cinechord.presentation.states.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()
    
    init {
        if (authRepository.isUserAuthenticated()) {
            processIntent(AuthIntent.GetCurrentUser)
        }
    }
    
    fun processIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.SignIn -> signIn(intent.email, intent.password)
            is AuthIntent.SignUp -> signUp(intent.name, intent.email, intent.password)
            is AuthIntent.SignOut -> signOut()
            is AuthIntent.GetCurrentUser -> getCurrentUser()
        }
    }
    
    private fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isSignInSuccessful = false) }
            
            val result = signInUseCase(email, password)
            
            result.fold(
                onSuccess = { user ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            user = user,
                            isAuthenticated = true,
                            isSignInSuccessful = true
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "An unknown error occurred",
                            isSignInSuccessful = false
                        )
                    }
                }
            )
        }
    }
    
    private fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isSignUpSuccessful = false) }
            
            val result = signUpUseCase(name, email, password)
            
            result.fold(
                onSuccess = { user ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            user = user,
                            isAuthenticated = true,
                            isSignUpSuccessful = true
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "An unknown error occurred",
                            isSignUpSuccessful = false
                        )
                    }
                }
            )
        }
    }
    
    private fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isSignOutSuccessful = false) }
            
            val result = authRepository.signOut()
            
            result.fold(
                onSuccess = {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            user = null,
                            isAuthenticated = false,
                            isSignOutSuccessful = true
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "An unknown error occurred",
                            isSignOutSuccessful = false
                        )
                    }
                }
            )
        }
    }
    
    private fun getCurrentUser() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            authRepository.getCurrentUser().collect { user ->
                _state.update { 
                    it.copy(
                        isLoading = false,
                        user = user,
                        isAuthenticated = user != null
                    )
                }
            }
        }
    }
    
    fun resetState() {
        _state.update {
            it.copy(
                error = null,
                isSignInSuccessful = false,
                isSignUpSuccessful = false,
                isSignOutSuccessful = false
            )
        }
    }
} 