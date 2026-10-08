package com.example

import android.os.Bundle
import java.io.File
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.EpisodeRepository
import com.example.model.Episode
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WatchScreen
import com.example.ui.theme.AnimeBallTheme

sealed class AppScreen {
    object Splash : AppScreen()
    object Home : AppScreen()
    data class Watch(val episodeId: Int) : AppScreen()
}

class MainActivity : ComponentActivity() {

    private lateinit var episodeRepository: EpisodeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val codeCacheDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache")
            File(codeCacheDir, "js").mkdirs()
            File(codeCacheDir, "wasm").mkdirs()
        } catch (ignored: Exception) {}

        episodeRepository = EpisodeRepository(this)
        enableEdgeToEdge()

        setContent {
            AnimeBallTheme {
                val episodes by episodeRepository.episodes.collectAsState()
                val lastWatchedId by episodeRepository.lastWatchedEpisodeId.collectAsState()

                var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                            when (screen) {
                                is AppScreen.Splash -> {
                                    SplashScreen(
                                        onSplashFinished = {
                                            currentScreen = AppScreen.Home
                                        }
                                    )
                                }

                                is AppScreen.Home -> {
                                    HomeScreen(
                                        episodes = episodes,
                                        lastWatchedId = lastWatchedId,
                                        onEpisodeSelected = { episode ->
                                            episodeRepository.markAsWatched(episode.id)
                                            currentScreen = AppScreen.Watch(episode.id)
                                        },
                                        onToggleFavorite = { episodeId ->
                                            episodeRepository.toggleFavorite(episodeId)
                                        },
                                        onCheckNewEpisodes = {
                                            episodeRepository.checkForNewEpisodes()
                                        }
                                    )
                                }

                                is AppScreen.Watch -> {
                                    val currentEp = episodes.find { it.id == screen.episodeId }
                                        ?: episodeRepository.getEpisodeById(screen.episodeId)
                                        ?: episodes.first()

                                    val prevEp = episodeRepository.getPreviousEpisode(currentEp.id)
                                    val nextEp = episodeRepository.getNextEpisode(currentEp.id)
                                    val suggested = episodeRepository.getSuggestedEpisodes(currentEp.id)

                                    WatchScreen(
                                        currentEpisode = currentEp,
                                        allEpisodes = episodes,
                                        previousEpisode = prevEp,
                                        nextEpisode = nextEp,
                                        suggestedEpisodes = suggested,
                                        onSelectEpisode = { targetEp ->
                                            episodeRepository.markAsWatched(targetEp.id)
                                            currentScreen = AppScreen.Watch(targetEp.id)
                                        },
                                        onToggleFavorite = { episodeId ->
                                            episodeRepository.toggleFavorite(episodeId)
                                        },
                                        onBack = {
                                            currentScreen = AppScreen.Home
                                        },
                                        onGetDirectUrl = { ep ->
                                            episodeRepository.getDirectMp4Url(ep)
                                        }
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
