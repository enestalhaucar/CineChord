package com.example.cinechord.presentation.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cinechord.domain.models.WatchRoom
import com.example.cinechord.presentation.intents.AuthIntent
import com.example.cinechord.presentation.intents.WatchRoomIntent
import com.example.cinechord.presentation.screens.home.components.CreateRoomDialog
import com.example.cinechord.presentation.viewmodels.AuthViewModel
import com.example.cinechord.presentation.viewmodels.HomeViewModel
import com.example.cinechord.presentation.viewmodels.RoomsUiState
import com.example.cinechord.presentation.viewmodels.WatchRoomViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRoomClick: (String) -> Unit,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel = hiltViewModel(),
    watchRoomViewModel: WatchRoomViewModel = hiltViewModel()
) {
    val authState by authViewModel.state.collectAsState()
    val exploreRoomsUiState by homeViewModel.roomsUiState.collectAsState()
    val userSpecificRoomsState by watchRoomViewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authState.user?.id, selectedTabIndex) {
        authState.user?.id?.let {
            when (selectedTabIndex) {
                0 -> { }
                1 -> watchRoomViewModel.processIntent(WatchRoomIntent.GetUserRooms(it))
                2 -> watchRoomViewModel.processIntent(WatchRoomIntent.GetUserCreatedRooms(it))
            }
        }
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (!authState.isAuthenticated && authState.user == null) {
        }
    }

    LaunchedEffect(exploreRoomsUiState, userSpecificRoomsState.error) {
        val exploreError = (exploreRoomsUiState as? RoomsUiState.Error)?.message
        val userSpecificError = userSpecificRoomsState.error

        val errorMessage = exploreError ?: userSpecificError
        if (errorMessage != null) {
            scope.launch {
                snackbarHostState.showSnackbar(message = errorMessage)
            }
            if (userSpecificError != null) watchRoomViewModel.resetState()
        }
    }

    LaunchedEffect(userSpecificRoomsState.isRoomCreated, userSpecificRoomsState.createdRoomId) {
        if (userSpecificRoomsState.isRoomCreated && userSpecificRoomsState.createdRoomId != null) {
            onRoomClick(userSpecificRoomsState.createdRoomId!!)
            watchRoomViewModel.processIntent(WatchRoomIntent.ResetRoomCreatedState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CineChord") },
                actions = {
                    IconButton(onClick = {
                        authState.user?.id?.let {
                            when (selectedTabIndex) {
                                0 -> { }
                                1 -> watchRoomViewModel.processIntent(WatchRoomIntent.GetUserRooms(it))
                                2 -> watchRoomViewModel.processIntent(WatchRoomIntent.GetUserCreatedRooms(it))
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Yenile")
                    }
                    IconButton(onClick = { authViewModel.processIntent(AuthIntent.SignOut) }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Çıkış Yap")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateRoomDialog = true }) {
                Icon(Icons.Filled.Add, "Oda Oluştur")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Keşfet") })
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Katıldıklarım") })
                Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }, text = { Text("Oluşturduklarım") })
            }

            when (selectedTabIndex) {
                0 -> {
                    when (val state = exploreRoomsUiState) {
                        is RoomsUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        is RoomsUiState.Success -> RoomListContent(state.rooms, "Henüz hiç oda oluşturulmamış.", onRoomClick)
                        is RoomsUiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Hata: ${state.message}") }
                    }
                }
                1 -> {
                    if (userSpecificRoomsState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        RoomListContent(userSpecificRoomsState.rooms, "Henüz hiçbir odaya katılmadınız.", onRoomClick)
                    }
                }
                2 -> {
                    if (userSpecificRoomsState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        RoomListContent(userSpecificRoomsState.rooms, "Henüz hiç oda oluşturmadınız.", onRoomClick)
                    }
                }
            }
        }
    }

    if (showCreateRoomDialog) {
        CreateRoomDialog(
            onCreateRoom = { name, description, videoUrl ->
                authState.user?.let { user ->
                    watchRoomViewModel.processIntent(
                        WatchRoomIntent.CreateRoom(
                            name = name,
                            description = description,
                            ownerId = user.id,
                            videoUrl = videoUrl
                        )
                    )
                }
                showCreateRoomDialog = false
            },
            onDismiss = { showCreateRoomDialog = false }
        )
    }
}

@Composable
fun RoomListContent(
    rooms: List<WatchRoom>,
    emptyMessage: String,
    onRoomClick: (String) -> Unit
) {
    if (rooms.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(emptyMessage)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
            items(rooms, key = { room -> room.id }) { room ->
                RoomListItem(room = room, onRoomClick = onRoomClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListItem(
    room: WatchRoom,
    onRoomClick: (String) -> Unit
) {
    ListItem(
        headlineContent = { Text(room.name) },
        supportingContent = { Text(room.description.ifEmpty { "Açıklama yok" }) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onRoomClick(room.id) }
    )
} 