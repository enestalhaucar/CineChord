package com.example.cinechord.presentation.screens.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.cinechord.domain.models.Message
import com.example.cinechord.domain.models.MessageType
import com.example.cinechord.presentation.intents.WatchRoomIntent
import com.example.cinechord.presentation.screens.watchroom.components.ChatMessage
import com.example.cinechord.presentation.viewmodels.AuthViewModel
import com.example.cinechord.presentation.viewmodels.WatchRoomViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMessageHistoryScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    watchRoomViewModel: WatchRoomViewModel = hiltViewModel()
) {
    val authState by authViewModel.state.collectAsState()
    val watchRoomState by watchRoomViewModel.state.collectAsState()
    
    var userMessages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Tüm kullanıcının katıldığı odalar ve mesajları getir
    LaunchedEffect(Unit) {
        if (authState.user != null) {
            watchRoomViewModel.processIntent(WatchRoomIntent.GetUserRooms(authState.user!!.id))
        }
    }
    
    // Odalar yüklendiğinde
    LaunchedEffect(watchRoomState.rooms) {
        isLoading = true
        userMessages = emptyList()
        
        if (watchRoomState.rooms.isNotEmpty()) {
            for (room in watchRoomState.rooms) {
                watchRoomViewModel.processIntent(WatchRoomIntent.GetMessages(room.id))
            }
        } else {
            isLoading = false
        }
    }
    
    // Mesajlar yüklendiğinde
    LaunchedEffect(watchRoomState.messages) {
        if (watchRoomState.messages.isNotEmpty() && authState.user != null) {
            userMessages = watchRoomState.messages.filter { 
                it.senderId == authState.user!!.id || 
                it.content.contains(authState.user!!.name) 
            }.sortedByDescending { it.timestamp }
            
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mesaj Geçmişim") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (userMessages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Henüz bir mesajınız yok",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(userMessages) { message ->
                        ChatMessage(
                            message = message,
                            isCurrentUser = message.senderId == authState.user?.id
                        )
                    }
                }
            }
        }
    }
} 