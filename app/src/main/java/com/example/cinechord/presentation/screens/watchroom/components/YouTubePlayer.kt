package com.example.cinechord.presentation.screens.watchroom.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubePlayer(
    modifier: Modifier = Modifier,
    videoId: String,
    currentPosition: Float = 0f,
    isPlaying: Boolean = true,
    onPlayerPositionChanged: (Float) -> Unit,
    onPlayerStateChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Keep track of player state
    var youTubePlayer by remember { mutableStateOf<YouTubePlayer?>(null) }
    var isPlayerReady by remember { mutableStateOf(false) }
    
    // Keep track of state changes
    var lastIsPlayingUpdate by remember { mutableStateOf(isPlaying) }
    var isUserInitiatedChange by remember { mutableStateOf(false) }
    var lastPositionUpdate by remember { mutableStateOf(currentPosition) }
    
    // Use rememberUpdatedState to avoid closures capturing old values
    val currentOnPlayerStateChanged by rememberUpdatedState(onPlayerStateChanged)
    val currentOnPlayerPositionChanged by rememberUpdatedState(onPlayerPositionChanged)

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (isPlaying && isPlayerReady) {
                        youTubePlayer?.play()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    if (isPlayerReady) {
                        youTubePlayer?.pause()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Update player state when isPlaying changes externally
    LaunchedEffect(isPlaying) {
        if (isPlayerReady && isPlaying != lastIsPlayingUpdate) {
            lastIsPlayingUpdate = isPlaying
            if (isPlaying) {
                youTubePlayer?.play()
            } else {
                youTubePlayer?.pause()
            }
        }
    }

    // Update player position when currentPosition changes externally
    LaunchedEffect(currentPosition) {
        if (isPlayerReady && youTubePlayer != null && 
            Math.abs(currentPosition - lastPositionUpdate) > 1.0f) {
            lastPositionUpdate = currentPosition
            youTubePlayer?.seekTo(currentPosition)
        }
    }

    // Update video ID when it changes
    LaunchedEffect(videoId) {
        if (isPlayerReady && youTubePlayer != null) {
            youTubePlayer?.loadVideo(videoId, currentPosition)
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            YouTubePlayerView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                lifecycleOwner.lifecycle.addObserver(this)

                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(player: YouTubePlayer) {
                        youTubePlayer = player
                        isPlayerReady = true
                        player.loadVideo(videoId, currentPosition)
                        if (!isPlaying) {
                            player.pause()
                        }
                        lastIsPlayingUpdate = isPlaying
                    }

                    override fun onStateChange(
                        player: YouTubePlayer,
                        state: PlayerConstants.PlayerState
                    ) {
                        val isPlaying = state == PlayerConstants.PlayerState.PLAYING
                        
                        // Only notify of state changes if it's different from the last update
                        // and isn't caused by our own LaunchedEffect
                        if (isPlaying != lastIsPlayingUpdate) {
                            isUserInitiatedChange = true
                            lastIsPlayingUpdate = isPlaying
                            
                            // Only notify if it's a user initiated change
                            if (isUserInitiatedChange) {
                                currentOnPlayerStateChanged(isPlaying)
                            }
                        }
                    }

                    override fun onCurrentSecond(player: YouTubePlayer, second: Float) {
                        // Update last position to avoid feedback loop
                        lastPositionUpdate = second
                        currentOnPlayerPositionChanged(second)
                    }
                })
            }
        }
    )
} 