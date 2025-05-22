package com.example.cinechord.presentation.screens.watchroom.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.example.cinechord.util.URLUtils

private const val TAG = "SmartVideoPlayer"

@UnstableApi
@Composable
fun SmartVideoPlayer(
    modifier: Modifier = Modifier,
    videoUrl: String,
    currentPosition: Long = 0L,
    isPlaying: Boolean = true,
    onPlayerPositionChanged: (Long) -> Unit,
    onPlayerStateChanged: (Boolean) -> Unit,
    roomPlaybackState: Boolean = false,
    isSyncingPlayback: Boolean = false
) {
    // Log video URL for debugging
    Log.d(TAG, "SmartVideoPlayer: videoUrl = $videoUrl")
    
    val urlType = remember(videoUrl) { 
        URLUtils.getUrlType(videoUrl) 
    }
    
    // Log URL type for debugging
    Log.d(TAG, "SmartVideoPlayer: urlType = $urlType")
    
    when (urlType) {
        "youtube" -> {
            val videoId = remember(videoUrl) { 
                URLUtils.extractVideoId(videoUrl) ?: ""
            }
            
            Log.d(TAG, "SmartVideoPlayer: YouTube videoId = $videoId")
            
            if (videoId.isNotEmpty()) {
                YouTubePlayer(
                    modifier = modifier,
                    videoId = videoId,
                    currentPosition = currentPosition / 1000f, // Convert from ms to seconds
                    isPlaying = isPlaying,
                    onPlayerPositionChanged = { 
                        onPlayerPositionChanged((it * 1000).toLong()) // Convert from seconds to ms
                    },
                    onPlayerStateChanged = onPlayerStateChanged
                )
            } else {
                Log.e(TAG, "SmartVideoPlayer: Invalid YouTube videoId")
            }
        }
        "streaming" -> {
            // For streaming sites, use WebView
            Log.d(TAG, "SmartVideoPlayer: Using WebView for streaming URL")
            WebViewPlayer(
                modifier = modifier,
                url = videoUrl,
                onPlayerStateChanged = onPlayerStateChanged
            )
        }
        else -> {
            // Use the regular video player for direct video URLs
            Log.d(TAG, "SmartVideoPlayer: Using regular VideoPlayer")
            VideoPlayer(
                modifier = modifier,
                videoUrl = videoUrl,
                currentPosition = currentPosition,
                isPlaying = isPlaying,
                onPlayerPositionChanged = onPlayerPositionChanged,
                onPlayerStateChanged = onPlayerStateChanged
            )
        }
    }
} 