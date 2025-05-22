package com.example.cinechord.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinechord.domain.models.WatchRoom
import com.example.cinechord.domain.usecase.watchroom.GetAllRoomsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface RoomsUiState {
    object Loading : RoomsUiState
    data class Success(val rooms: List<WatchRoom>) : RoomsUiState
    data class Error(val message: String) : RoomsUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllRoomsUseCase: GetAllRoomsUseCase
) : ViewModel() {

    val roomsUiState: StateFlow<RoomsUiState> =
        getAllRoomsUseCase()
            .map<List<WatchRoom>, RoomsUiState> { rooms -> RoomsUiState.Success(rooms) }
            .catch { throwable -> emit(RoomsUiState.Error(throwable.message ?: "Oda listesi alınırken bir hata oluştu.")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = RoomsUiState.Loading
            )
} 