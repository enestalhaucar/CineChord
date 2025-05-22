package com.example.cinechord.util

import java.util.regex.Pattern

object URLUtils {
    
    // YouTube URL patterns
    private val YOUTUBE_URL_PATTERNS = listOf(
        // Standard youtube.com URL with v parameter
        Pattern.compile("(?:youtube\\.com\\/watch\\?v=)([\\w-]+)"),
        // youtu.be shortened URLs
        Pattern.compile("(?:youtu\\.be\\/)([\\w-]+)"),
        // Mobile youtube URLs
        Pattern.compile("(?:youtube\\.com\\/embed\\/)([\\w-]+)"),
        // youtube.com/v/ URLs
        Pattern.compile("(?:youtube\\.com\\/v\\/)([\\w-]+)"),
        // youtube.com/shorts/ URLs
        Pattern.compile("(?:youtube\\.com\\/shorts\\/)([\\w-]+)")
    )
    
    /**
     * Extracts the video ID from a YouTube URL.
     * 
     * @param url The YouTube URL
     * @return The video ID, or null if the URL is not a valid YouTube URL
     */
    fun extractVideoId(url: String): String? {
        for (pattern in YOUTUBE_URL_PATTERNS) {
            val matcher = pattern.matcher(url)
            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        return null
    }
    
    /**
     * Checks if a URL is a YouTube URL.
     * 
     * @param url The URL to check
     * @return True if the URL is a YouTube URL
     */
    fun isYouTubeUrl(url: String): Boolean {
        return url.contains("youtube.com") || url.contains("youtu.be")
    }
    
    /**
     * Checks if a URL is from a streaming site that requires WebView.
     * 
     * @param url The URL to check
     * @return True if the URL is from a streaming site
     */
    fun isStreamingSiteUrl(url: String): Boolean {
        val streamingSiteDomains = listOf(
            "dizipal", "sezonlukdizi", "hdfilmcehennemi", "netflix", 
            "dizilla", "filmmodu", "blutvlogo", "puhu", "exxen", "mubi", 
            "primevideo", "disney", "hulu", "hbo", "twitch", "dailymotion",
            "vimeo", "blutv", "beyazperde"
        )
        
        return streamingSiteDomains.any { url.contains(it, ignoreCase = true) }
    }
    
    /**
     * Determines if the URL needs special handling.
     * 
     * @param url The URL to check
     * @return URL type: "youtube", "streaming", or "direct"
     */
    fun getUrlType(url: String): String {
        return when {
            isYouTubeUrl(url) -> "youtube"
            isStreamingSiteUrl(url) -> "streaming"
            else -> "direct"
        }
    }
} 