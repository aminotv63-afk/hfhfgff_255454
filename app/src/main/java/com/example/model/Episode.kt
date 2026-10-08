package com.example.model

data class Episode(
    val id: Int,
    val title: String,
    val seriesName: String = "دراغون بول سوبر",
    val embedUrl: String,
    val durationText: String = "24:00",
    val description: String,
    val isWatched: Boolean = false,
    val isFavorite: Boolean = false
)
