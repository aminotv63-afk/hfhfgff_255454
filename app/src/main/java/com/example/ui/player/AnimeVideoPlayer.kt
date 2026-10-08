package com.example.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.Episode
import com.example.ui.theme.AnimeGold
import com.example.ui.theme.AnimeGoldLight
import com.example.ui.theme.AnimeRed
import com.example.ui.theme.AnimeRedBright
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

enum class ServerType {
    DIRECT, // Fast clean HTML5 direct mp4 stream
    EMBED   // Official 4s.io embed player
}

class VideoBridge(
    private val onTimeUpdate: (currentTime: Float, duration: Float) -> Unit,
    private val onPlayStateChanged: (isPlaying: Boolean) -> Unit
) {
    @JavascriptInterface
    fun updateTime(current: Float, total: Float) {
        onTimeUpdate(current, total)
    }

    @JavascriptInterface
    fun updatePlayState(playing: Boolean) {
        onPlayStateChanged(playing)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AnimeVideoPlayer(
    episode: Episode,
    isFullscreen: Boolean,
    onToggleFullscreen: (Boolean) -> Unit,
    onPreviousEpisode: (() -> Unit)?,
    onNextEpisode: (() -> Unit)?,
    onBackClick: () -> Unit,
    onGetDirectUrl: (suspend (Episode) -> String?)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var serverType by remember { mutableStateOf(ServerType.DIRECT) }
    var directMp4Url by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentTimeSec by remember { mutableFloatStateOf(0f) }
    var durationSec by remember { mutableFloatStateOf(1440f) }
    var currentSpeed by remember { mutableFloatStateOf(1.0f) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    // Resolve direct MP4 URL when episode changes
    LaunchedEffect(episode.id) {
        isLoading = true
        if (onGetDirectUrl != null) {
            val resolved = onGetDirectUrl(episode)
            directMp4Url = resolved
        }
        isLoading = false
    }

    // Auto-hide controls timer
    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            delay(5000)
            isControlsVisible = false
        }
    }

    BackHandler(enabled = isFullscreen) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onToggleFullscreen(false)
    }

    val isLandscape = activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun toggleOrientation() {
        val currentlyLandscape = activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (currentlyLandscape) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            onToggleFullscreen(false)
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            onToggleFullscreen(true)
        }
    }

    fun runJs(code: String) {
        webViewRef?.evaluateJavascript(code, null)
    }

    fun seekRelative(seconds: Int) {
        val script = """
            (function() {
                var v = document.querySelector('video');
                if (v) {
                    v.currentTime = Math.max(0, Math.min(v.duration || 1440, v.currentTime + ($seconds)));
                }
            })();
        """.trimIndent()
        runJs(script)
    }

    fun seekToFraction(fraction: Float) {
        val targetSec = fraction * durationSec
        currentTimeSec = targetSec
        val script = """
            (function() {
                var v = document.querySelector('video');
                if (v && v.duration) {
                    v.currentTime = v.duration * $fraction;
                }
            })();
        """.trimIndent()
        runJs(script)
    }

    fun togglePlayState() {
        val nextState = !isPlaying
        isPlaying = nextState
        val script = """
            (function() {
                var v = document.querySelector('video');
                if (v) {
                    if (v.paused) { v.play(); } else { v.pause(); }
                }
            })();
        """.trimIndent()
        runJs(script)
    }

    fun setSpeed(speed: Float) {
        currentSpeed = speed
        val script = """
            (function() {
                var v = document.querySelector('video');
                if (v) {
                    v.playbackRate = $speed;
                }
            })();
        """.trimIndent()
        runJs(script)
    }

    fun formatTime(seconds: Float): String {
        val totalSecs = seconds.toInt().coerceAtLeast(0)
        val minutes = totalSecs / 60
        val remainingSecs = totalSecs % 60
        return String.format("%02d:%02d", minutes, remainingSecs)
    }

    fun buildDirectPlayerHtml(url: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
              <style>
                * { box-sizing: border-box; }
                html, body {
                  margin: 0; padding: 0; width: 100%; height: 100%;
                  background-color: #000; overflow: hidden;
                  display: flex; align-items: center; justify-content: center;
                }
                video {
                  width: 100%; height: 100%; object-fit: contain; background: #000;
                }
              </style>
            </head>
            <body>
              <video id="vplayer" src="$url" controls playsinline autoplay></video>
              <script>
                var v = document.getElementById('vplayer');
                v.play().catch(function(){});
                v.addEventListener('timeupdate', function() {
                  if (window.AndroidBridge) {
                    window.AndroidBridge.updateTime(v.currentTime, v.duration || 1440);
                  }
                });
                v.addEventListener('play', function() {
                  if (window.AndroidBridge) { window.AndroidBridge.updatePlayState(true); }
                });
                v.addEventListener('pause', function() {
                  if (window.AndroidBridge) { window.AndroidBridge.updatePlayState(false); }
                });
              </script>
            </body>
            </html>
        """.trimIndent()
    }

    fun loadContentIntoWebView(wv: WebView) {
        if (serverType == ServerType.DIRECT && directMp4Url != null) {
            val html = buildDirectPlayerHtml(directMp4Url!!)
            wv.loadDataWithBaseURL("https://www.4s.io", html, "text/html", "UTF-8", null)
        } else {
            wv.loadUrl(episode.embedUrl)
        }
    }

    Box(
        modifier = modifier
            .testTag("anime_video_player_container")
            .then(
                if (isFullscreen) Modifier.fillMaxSize()
                else Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
            .background(Color.Black)
    ) {
        // Embed WebView - Directly handles touches without blocking Compose click listeners
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                try {
                    val codeCacheDir = File(ctx.cacheDir, "WebView/Default/HTTP Cache/Code Cache")
                    File(codeCacheDir, "js").mkdirs()
                    File(codeCacheDir, "wasm").mkdirs()
                } catch (ignored: Exception) {}

                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Enable Cookies (including cross-domain third-party cookies for 4shared video streams)
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        // Use standard Chrome Mobile User Agent to prevent blocks from embed providers
                        userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                        setSupportMultipleWindows(false)
                        javaScriptCanOpenWindowsAutomatically = false
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    val bridge = VideoBridge(
                        onTimeUpdate = { cur, dur ->
                            currentTimeSec = cur
                            if (dur > 0) durationSec = dur
                        },
                        onPlayStateChanged = { playing ->
                            isPlaying = playing
                        }
                    )
                    addJavascriptInterface(bridge, "AndroidBridge")

                    webViewClient = object : WebViewClient() {
                        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                            return true
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val url = request?.url?.toString() ?: ""
                            // Prevent redirecting away from video to advertising networks
                            return if (url.contains("4s.io") || url.contains("4shared.com") || url.contains(".mp4")) {
                                false
                            } else {
                                true // Block popups/ad redirects
                            }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            // Inject script to start playback and hook events
                            val initScript = """
                                (function() {
                                    function autoPlayVideo() {
                                        var v = document.querySelector('video');
                                        if (v) {
                                            v.muted = false;
                                            v.controls = true;
                                            v.play().catch(function(){});
                                            v.addEventListener('timeupdate', function() {
                                                if (window.AndroidBridge) {
                                                    window.AndroidBridge.updateTime(v.currentTime, v.duration || 1440);
                                                }
                                            });
                                            v.addEventListener('play', function() {
                                                if (window.AndroidBridge) { window.AndroidBridge.updatePlayState(true); }
                                            });
                                            v.addEventListener('pause', function() {
                                                if (window.AndroidBridge) { window.AndroidBridge.updatePlayState(false); }
                                            });
                                        }
                                        var btn = document.querySelector('.vjs-big-play-button');
                                        if (btn) { btn.click(); }
                                    }
                                    autoPlayVideo();
                                    setTimeout(autoPlayVideo, 1000);
                                    setTimeout(autoPlayVideo, 2500);
                                })();
                            """.trimIndent()
                            view?.evaluateJavascript(initScript, null)
                        }
                    }

                    webChromeClient = object : WebChromeClient() {}

                    loadContentIntoWebView(this)
                    webViewRef = this
                }
            },
            update = { wv ->
                webViewRef = wv
            }
        )

        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        color = AnimeRed,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "جاري تجهيز مشغل الحلقة...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }

        // Floating Control Toggle Button (Top right corner, unobtrusive)
        IconButton(
            onClick = { isControlsVisible = !isControlsVisible },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                .testTag("toggle_controls_button")
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "التحكم بالمشغل",
                tint = if (isControlsVisible) AnimeRedBright else Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        // Overlay Controls (Docked cleanly at Top and Bottom without blocking the center video touch area)
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top Control Bar
                Surface(
                    color = Color.Black.copy(alpha = 0.85f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = {
                                    if (isFullscreen) {
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                        onToggleFullscreen(false)
                                    } else {
                                        onBackClick()
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface)
                                    .testTag("player_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "الحلقة ${episode.id}: ${episode.title}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (serverType == ServerType.DIRECT) "سيرفر مباشر فائق السرعة" else "سيرفر التضمين 4s.io",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AnimeGold
                                )
                            }
                        }

                        // Actions: Reload, External Player, Fullscreen
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Reload
                            IconButton(
                                onClick = {
                                    webViewRef?.let { loadContentIntoWebView(it) }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface)
                                    .testTag("player_reload_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "إعادة تحميل",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Open in external browser / player
                            IconButton(
                                onClick = {
                                    val targetUrl = directMp4Url ?: episode.embedUrl
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                    context.startActivity(Intent.createChooser(intent, "تشغيل في تطبيق خارجي"))
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface)
                                    .testTag("player_external_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "فتح في مشغل خارجي",
                                    tint = AnimeGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Orientation Flip Button (تقليب أفقي / عمودي)
                            IconButton(
                                onClick = { toggleOrientation() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isLandscape) AnimeRed else DarkSurface)
                                    .testTag("player_flip_orientation_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenRotation,
                                    contentDescription = "تقليب الشاشة (أفقي / عمودي)",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Fullscreen
                            IconButton(
                                onClick = {
                                    val nextFs = !isFullscreen
                                    onToggleFullscreen(nextFs)
                                    if (nextFs) {
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                    } else {
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface)
                                    .testTag("player_fullscreen_button")
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "ملء الشاشة",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom Control Bar
                Surface(
                    color = Color.Black.copy(alpha = 0.9f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        // Time & Seekbar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(currentTimeSec),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            // Slim Seekbar
                            Slider(
                                value = if (durationSec > 0) (currentTimeSec / durationSec).coerceIn(0f, 1f) else 0f,
                                onValueChange = { fraction ->
                                    seekToFraction(fraction)
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = AnimeRedBright,
                                    activeTrackColor = AnimeRed,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                                    .height(24.dp)
                                    .testTag("player_seekbar")
                            )

                            Text(
                                text = formatTime(durationSec),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        // Control Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left buttons: Prev, Rewind 10, Play/Pause, Forward 10, Next
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (onPreviousEpisode != null) {
                                    IconButton(
                                        onClick = onPreviousEpisode,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SkipPrevious,
                                            contentDescription = "السابقة",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { seekRelative(-10) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Replay10,
                                        contentDescription = "-10 ثوانٍ",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { togglePlayState() },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(AnimeRed)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { seekRelative(10) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Forward10,
                                        contentDescription = "+10 ثوانٍ",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (onNextEpisode != null) {
                                    IconButton(
                                        onClick = onNextEpisode,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SkipNext,
                                            contentDescription = "التالية",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Right buttons: Speed selector + Server Switcher
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Speed Selector
                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DarkSurface,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { showSpeedMenu = true }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${currentSpeed}x",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = AnimeGold
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showSpeedMenu,
                                        onDismissRequest = { showSpeedMenu = false }
                                    ) {
                                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = "${speed}x",
                                                        fontWeight = if (speed == currentSpeed) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (speed == currentSpeed) AnimeRed else Color.White
                                                    )
                                                },
                                                onClick = {
                                                    setSpeed(speed)
                                                    showSpeedMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Server Mode Toggle (Direct / Embed)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (serverType == ServerType.DIRECT) AnimeRed else DarkSurface,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            val nextMode = if (serverType == ServerType.DIRECT) ServerType.EMBED else ServerType.DIRECT
                                            serverType = nextMode
                                            webViewRef?.let { loadContentIntoWebView(it) }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("switch_server_button")
                                ) {
                                    Text(
                                        text = if (serverType == ServerType.DIRECT) "سيرفر مباشر" else "سيرفر التضمين",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }

                                // Quick Flip Chip (أفقي / عمودي)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isLandscape) AnimeRed.copy(alpha = 0.8f) else DarkSurface,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { toggleOrientation() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("quick_flip_orientation_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ScreenRotation,
                                            contentDescription = null,
                                            tint = if (isLandscape) Color.White else AnimeGold,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = if (isLandscape) "عمودي" else "أفقي",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(episode.id, serverType, directMp4Url) {
        webViewRef?.let { wv ->
            loadContentIntoWebView(wv)
        }
        onDispose {
            webViewRef?.let { wv ->
                try {
                    wv.stopLoading()
                    wv.loadUrl("about:blank")
                } catch (ignored: Exception) {}
            }
        }
    }
}
