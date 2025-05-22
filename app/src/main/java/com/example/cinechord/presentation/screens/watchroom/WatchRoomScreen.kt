package com.example.cinechord.presentation.screens.watchroom

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.example.cinechord.domain.models.MessageType
import com.example.cinechord.presentation.intents.WatchRoomIntent
import com.example.cinechord.presentation.screens.watchroom.components.ChatSection
import com.example.cinechord.presentation.screens.watchroom.components.LandscapeChat
import com.example.cinechord.presentation.screens.watchroom.components.SmartVideoPlayer
import com.example.cinechord.presentation.viewmodels.AuthViewModel
import com.example.cinechord.presentation.viewmodels.WatchRoomViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.TextButton

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchRoomScreen(
    navController: NavHostController,
    roomId: String,
    authViewModel: AuthViewModel,
    watchRoomViewModel: WatchRoomViewModel = hiltViewModel()
) {
    val authState by authViewModel.state.collectAsState()
    val watchRoomState by watchRoomViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var isVideoUrlDialogVisible by remember { mutableStateOf(false) }
    var isSyncingPlayback by remember { mutableStateOf(false) }
    var showChatPanel by remember { mutableStateOf(false) }
    
    // Detect screen orientation
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    // Load room and messages
    LaunchedEffect(roomId) {
        Log.d("WatchRoomScreen", "Loading room with ID: $roomId")
        watchRoomViewModel.processIntent(WatchRoomIntent.GetRoomById(roomId))
        watchRoomViewModel.processIntent(WatchRoomIntent.GetMessages(roomId))
        
        // Join room
        authState.user?.let { user ->
            Log.d("WatchRoomScreen", "Joining room with user: ${user.id}")
            watchRoomViewModel.processIntent(
                WatchRoomIntent.JoinRoom(roomId, user.id)
            )
        }
    }
    
    // Reset message sent flag to avoid unwanted refreshes
    LaunchedEffect(watchRoomState.isMessageSent) {
        if (watchRoomState.isMessageSent) {
            // Wait a short time and reset the message sent flag
            delay(300)
            watchRoomViewModel.processIntent(WatchRoomIntent.ResetMessageSentState)
        }
    }
    
    // Log room state changes
    LaunchedEffect(watchRoomState.currentRoom) {
        val room = watchRoomState.currentRoom
        if (room != null) {
            Log.d("WatchRoomScreen", "Room loaded: ${room.name}")
            Log.d("WatchRoomScreen", "Video URL: ${room.videoUrl}")
            Log.d("WatchRoomScreen", "Is playing: ${room.isPlaying}")
            Log.d("WatchRoomScreen", "Current timestamp: ${room.currentTimestamp}")
            Log.d("WatchRoomScreen", "Participants: ${room.participants.size}")
        } else {
            Log.d("WatchRoomScreen", "Room is null")
        }
    }
    
    // Handle errors
    LaunchedEffect(watchRoomState.error) {
        if (watchRoomState.error != null) {
            Log.e("WatchRoomScreen", "Error: ${watchRoomState.error}")
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = watchRoomState.error ?: "An error occurred"
                )
            }
            watchRoomViewModel.resetState()
        }
    }
    
    // Set up navigation on room leave or delete
    LaunchedEffect(watchRoomState.isRoomLeft, watchRoomState.isRoomDeleted) {
        if (watchRoomState.isRoomLeft || watchRoomState.isRoomDeleted) {
            navController.popBackStack()
            watchRoomViewModel.resetState()
        }
    }
    
    // Clean up when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            // Leave room when navigating away
            authState.user?.let { user ->
                scope.launch {
                    watchRoomViewModel.processIntent(
                        WatchRoomIntent.LeaveRoom(roomId, user.id)
                    )
                }
            }
        }
    }
    
    // Change video url
    LaunchedEffect(watchRoomState.isVideoUrlUpdated) {
        if (watchRoomState.isVideoUrlUpdated) {
            // Reset state
            watchRoomViewModel.processIntent(WatchRoomIntent.ResetVideoUrlUpdatedState)
        }
    }
    
    Scaffold(
        topBar = {
            // Only show topbar in portrait mode
            if (!isLandscape) {
                TopAppBar(
                    title = { 
                        Text(
                            text = watchRoomState.currentRoom?.name ?: "Loading..."
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        // "İzle" butonu
                        watchRoomState.currentRoom?.videoUrl?.let { videoUrl ->
                            if (videoUrl.isNotBlank()) {
                                IconButton(onClick = {
                                    extractYouTubeVideoId(videoUrl)?.let { videoId ->
                                        navController.navigate("youtube_player/$videoId")
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Videoyu İzle"
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${watchRoomState.currentRoom?.participants?.size ?: 0}",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Participants"
                        )
                        
                        IconButton(onClick = { isVideoUrlDialogVisible = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Change Video"
                            )
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (watchRoomState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (watchRoomState.currentRoom == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Room not found",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        } else {
            val room = watchRoomState.currentRoom!!
            
            if (isLandscape) {
                // Landscape layout
                val paddingValue = if (!isLandscape) innerPadding else PaddingValues(0.dp)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValue)
                ) {
                    // Video oynatıcı direkt olarak burada
                    if (room.videoUrl.isBlank()) {
                        // Video URL boş ise hata mesajı göster
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Henüz video yok",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(onClick = { isVideoUrlDialogVisible = true }) {
                                    Text("Video Ekle")
                                }
                            }
                        }
                    } else {
                        // Video URL varsa oynatıcıyı göster
                        SmartVideoPlayer(
                            modifier = Modifier.fillMaxSize(),
                            videoUrl = room.videoUrl,
                            currentPosition = room.currentTimestamp,
                            isPlaying = room.isPlaying,
                            onPlayerPositionChanged = { position ->
                                if (!isSyncingPlayback) {
                                    watchRoomViewModel.processIntent(
                                        WatchRoomIntent.UpdatePlaybackState(
                                            roomId = roomId,
                                            timestamp = position,
                                            isPlaying = room.isPlaying
                                        )
                                    )
                                }
                            },
                            onPlayerStateChanged = { playing ->
                                if (!isSyncingPlayback && playing != room.isPlaying) {
                                    // Set syncing flag to avoid feedback loop
                                    isSyncingPlayback = true
                                    
                                    watchRoomViewModel.processIntent(
                                        WatchRoomIntent.UpdatePlaybackState(
                                            roomId = roomId,
                                            timestamp = room.currentTimestamp,
                                            isPlaying = playing
                                        )
                                    )
                                    
                                    // Send system message about play/pause
                                    authState.user?.let { user ->
                                        val message = if (playing) "▶️ ${user.name} started playback" 
                                            else "⏸️ ${user.name} paused playback"
                                        
                                        watchRoomViewModel.processIntent(
                                            WatchRoomIntent.SendMessage(
                                                roomId = roomId,
                                                senderId = "system",
                                                senderName = "System",
                                                senderProfileImage = "",
                                                content = message,
                                                type = MessageType.SYSTEM.name
                                            )
                                        )
                                    }
                                    
                                    // Reset syncing flag after a longer delay
                                    scope.launch {
                                        withContext(Dispatchers.Main) {
                                            kotlinx.coroutines.delay(2000)
                                            isSyncingPlayback = false
                                        }
                                    }
                                }
                            },
                            roomPlaybackState = room.isPlaying,
                            isSyncingPlayback = isSyncingPlayback
                        )
                    }
                    
                    // Floating action buttons and chat overlay
                    LandscapeChat(
                        messages = watchRoomState.messages,
                        currentUser = authState.user,
                        isMessageLoading = watchRoomState.isMessageLoading,
                        showChatPanel = showChatPanel,
                        onToggleChatPanel = { showChatPanel = !showChatPanel },
                        onSendMessage = { message ->
                            authState.user?.let { user ->
                                watchRoomViewModel.processIntent(
                                    WatchRoomIntent.SendMessage(
                                        roomId = roomId,
                                        senderId = user.id,
                                        senderName = user.name,
                                        senderProfileImage = user.profileImageUrl,
                                        content = message,
                                        type = MessageType.TEXT.name
                                    )
                                )
                            }
                        }
                    )
                }
            } else {
                // Portrait layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Video URL boş ise bilgilendirme mesajı göster
                        if (room.videoUrl.isBlank()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Henüz video yok",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(onClick = { isVideoUrlDialogVisible = true }) {
                                    Text("Video Ekle")
                                }
                            }
                        } else {
                            // Video URL varsa oynatıcıyı göster
                            SmartVideoPlayer(
                                modifier = Modifier.fillMaxSize(),
                                videoUrl = room.videoUrl,
                                currentPosition = room.currentTimestamp,
                                isPlaying = room.isPlaying,
                                onPlayerPositionChanged = { position ->
                                    if (!isSyncingPlayback) {
                                        watchRoomViewModel.processIntent(
                                            WatchRoomIntent.UpdatePlaybackState(
                                                roomId = roomId,
                                                timestamp = position,
                                                isPlaying = room.isPlaying
                                            )
                                        )
                                    }
                                },
                                onPlayerStateChanged = { playing ->
                                    if (!isSyncingPlayback && playing != room.isPlaying) {
                                        // Set syncing flag to avoid feedback loop
                                        isSyncingPlayback = true
                                        
                                        watchRoomViewModel.processIntent(
                                            WatchRoomIntent.UpdatePlaybackState(
                                                roomId = roomId,
                                                timestamp = room.currentTimestamp,
                                                isPlaying = playing
                                            )
                                        )
                                        
                                        // Send system message about play/pause
                                        authState.user?.let { user ->
                                            val message = if (playing) "▶️ ${user.name} started playback" 
                                                else "⏸️ ${user.name} paused playback"
                                            
                                            watchRoomViewModel.processIntent(
                                                WatchRoomIntent.SendMessage(
                                                    roomId = roomId,
                                                    senderId = "system",
                                                    senderName = "System",
                                                    senderProfileImage = "",
                                                    content = message,
                                                    type = MessageType.SYSTEM.name
                                                )
                                            )
                                        }
                                        
                                        // Reset syncing flag after a longer delay
                                        scope.launch {
                                            withContext(Dispatchers.Main) {
                                                kotlinx.coroutines.delay(2000)
                                                isSyncingPlayback = false
                                            }
                                        }
                                    }
                                },
                                roomPlaybackState = room.isPlaying,
                                isSyncingPlayback = isSyncingPlayback
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Chat section
                    ChatSection(
                        messages = watchRoomState.messages,
                        currentUser = authState.user,
                        isMessageLoading = watchRoomState.isMessageLoading,
                        onSendMessage = { message ->
                            authState.user?.let { user ->
                                watchRoomViewModel.processIntent(
                                    WatchRoomIntent.SendMessage(
                                        roomId = roomId,
                                        senderId = user.id,
                                        senderName = user.name,
                                        senderProfileImage = user.profileImageUrl,
                                        content = message,
                                        type = MessageType.TEXT.name
                                    )
                                )
                            }
                        }
                    )
                }
            }
            
            // Video URL change dialog
            if (isVideoUrlDialogVisible) {
                ChangeVideoUrlDialog(
                    currentUrl = room.videoUrl,
                    onConfirm = { newUrl ->
                        watchRoomViewModel.processIntent(
                            WatchRoomIntent.UpdateVideoUrl(
                                roomId = roomId,
                                videoUrl = newUrl
                            )
                        )
                        
                        // Send system message about video change
                        authState.user?.let { user ->
                            watchRoomViewModel.processIntent(
                                WatchRoomIntent.SendMessage(
                                    roomId = roomId,
                                    senderId = "system",
                                    senderName = "System",
                                    senderProfileImage = "",
                                    content = "🎬 ${user.name} changed the video",
                                    type = MessageType.SYSTEM.name
                                )
                            )
                        }
                        
                        isVideoUrlDialogVisible = false
                    },
                    onDismiss = { isVideoUrlDialogVisible = false }
                )
            }
        }
    }
}

@Composable
fun ChangeVideoUrlDialog(
    currentUrl: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var videoUrl by remember { mutableStateOf(currentUrl) }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Video URL") },
        text = {
            Column {
                androidx.compose.material3.OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    label = { Text("Video URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• YouTube videoları için: YouTube URL'si (örn. https://www.youtube.com/watch?v=...)\n\n" +
                           "• Streaming siteleri için: Dizipal, Sezonlukdizi, BluTV, vb. site URL'leri\n\n" +
                           "• Doğrudan videolar için: .mp4, .webm uzantılı dosya URL'leri",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = { onConfirm(videoUrl) },
                enabled = videoUrl.isNotBlank()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper function to extract YouTube Video ID from URL
fun extractYouTubeVideoId(youtubeUrl: String): String? {
    val pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*"
    val compiledPattern = Regex(pattern)
    val matcher = compiledPattern.find(youtubeUrl)
    return matcher?.value
} 