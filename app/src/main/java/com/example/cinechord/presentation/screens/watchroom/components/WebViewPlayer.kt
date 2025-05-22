package com.example.cinechord.presentation.screens.watchroom.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewPlayer(
    modifier: Modifier = Modifier,
    url: String,
    onPlayerStateChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    // Enable JavaScript and other required settings
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowContentAccess = true
                        allowFileAccess = true
                        databaseEnabled = true
                        loadsImagesAutomatically = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        mediaPlaybackRequiresUserGesture = false
                        setSupportMultipleWindows(true)
                        setGeolocationEnabled(false)
                        userAgentString = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
                    }
                    
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            if (newProgress >= 100) {
                                isLoading = false
                            }
                        }
                    }
                    
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                        }
                        
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            onPlayerStateChanged(true) // Assume playing when loaded
                            
                            // Inject JavaScript to make video fill the WebView and auto-play
                            evaluateJavascript("""
                                (function() {
                                    var videos = document.getElementsByTagName('video');
                                    for(var i=0; i<videos.length; i++) {
                                        videos[i].style.width = '100%';
                                        videos[i].style.height = '100%';
                                        videos[i].setAttribute('playsinline', '');
                                        videos[i].setAttribute('autoplay', '');
                                        videos[i].play();
                                    }
                                    
                                    var iframes = document.getElementsByTagName('iframe');
                                    for(var i=0; i<iframes.length; i++) {
                                        iframes[i].style.width = '100%';
                                        iframes[i].style.height = '100%';
                                    }
                                })();
                            """.trimIndent(), null)
                        }
                        
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            // Allow same-domain redirects
                            return false
                        }
                    }
                    
                    loadUrl(url)
                }
            }
        )
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            // Clean up resources if needed
        }
    }
} 