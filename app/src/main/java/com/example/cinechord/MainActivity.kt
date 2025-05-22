package com.example.cinechord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cinechord.presentation.screens.auth.AuthScreen
import com.example.cinechord.presentation.screens.home.HomeScreen
import com.example.cinechord.presentation.screens.watchroom.WatchRoomScreen
import com.example.cinechord.presentation.screens.messages.UserMessageHistoryScreen
import com.example.cinechord.presentation.screens.watchroom.YouTubePlayerScreen
import com.example.cinechord.presentation.viewmodels.AuthViewModel
import com.example.cinechord.ui.theme.CineChordTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CineChordTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val authState by authViewModel.state.collectAsState()
                    
                    Scaffold { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = if (authState.isAuthenticated) "home" else "auth",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("auth") {
                                AuthScreen(
                                    navController = navController,
                                    viewModel = authViewModel
                                )
                            }
                            
                            composable("home") {
                                HomeScreen(
                                    onRoomClick = { roomId ->
                                        navController.navigate("watchroom/$roomId")
                                    },
                                    authViewModel = authViewModel
                                )
                            }
                            
                            composable("watchroom/{roomId}") { backStackEntry ->
                                val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                                WatchRoomScreen(
                                    navController = navController,
                                    roomId = roomId,
                                    authViewModel = authViewModel
                                )
                            }
                            
                            composable("message-history") {
                                UserMessageHistoryScreen(
                                    navController = navController,
                                    authViewModel = authViewModel
                                )
                            }

                            composable(
                                route = "youtube_player/{videoId}",
                                arguments = listOf(navArgument("videoId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
                                YouTubePlayerScreen(
                                    navController = navController,
                                    videoId = videoId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}