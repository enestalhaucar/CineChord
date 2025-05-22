package com.example.cinechord.presentation.screens.watchroom.components

import android.net.Uri
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@UnstableApi
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoUrl: String,
    currentPosition: Long,
    isPlaying: Boolean,
    onPlayerPositionChanged: (Long) -> Unit,
    onPlayerStateChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    
    // Keep track of last external isPlaying change to avoid feedback loops
    var lastIsPlayingUpdate by remember { mutableStateOf(isPlaying) }
    // Remember if the change was internal or external
    var isUserInitiatedChange by remember { mutableStateOf(false) }
    
    val currentOnPlayerStateChanged by rememberUpdatedState(onPlayerStateChanged)
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = isPlaying
            setMediaItem(MediaItem.fromUri(videoUrl.toUri()))
            prepare()
        }
    }
    
    // Sync player with external controls (isPlaying)
    LaunchedEffect(isPlaying) {
        if (isPlaying != lastIsPlayingUpdate) {
            lastIsPlayingUpdate = isPlaying
            exoPlayer.playWhenReady = isPlaying
        }
    }
    
    // Sync player with external position changes
    LaunchedEffect(currentPosition) {
        val diff = abs(exoPlayer.currentPosition - currentPosition)
        if (diff > 1000) { // Only seek if difference is more than 1 second
            exoPlayer.seekTo(currentPosition)
        }
    }
    
    // Update video source when URL changes
    LaunchedEffect(videoUrl) {
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl.toUri()))
        exoPlayer.prepare()
    }
    
    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    onPlayerPositionChanged(exoPlayer.currentPosition)
                    // Only notify of state changes if it's a user initiated change
                    if (isUserInitiatedChange) {
                        currentOnPlayerStateChanged(exoPlayer.isPlaying)
                        isUserInitiatedChange = false
                    }
                }
            }
            
            override fun onIsPlayingChanged(playing: Boolean) {
                // Check if this is a user initiated change (not from our LaunchedEffect)
                if (playing != lastIsPlayingUpdate) {
                    isUserInitiatedChange = true
                    lastIsPlayingUpdate = playing
                }
                
                // Only notify of state changes if it's a user initiated change
                if (isUserInitiatedChange) {
                    currentOnPlayerStateChanged(playing)
                }
            }
        }
        
        exoPlayer.addListener(listener)
        
        // Periodically update position (less frequently)
        val positionUpdateJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(2000) // Update every 2 seconds instead of every second
                if (exoPlayer.isPlaying) {
                    onPlayerPositionChanged(exoPlayer.currentPosition)
                }
            }
        }
        
        onDispose {
            positionUpdateJob.cancel()
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }
    
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                useController = true
                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            }
        }
    )
} 