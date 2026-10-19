package com.example.appdivulga.ui.feed

import com.example.appdivulga.data.model.Post
import com.example.appdivulga.data.model.Profile
import com.example.appdivulga.location.Coordinates

enum class FeedMode {
    RECEBEDOR,  // Feed de ofertas próximas filtrado por interesses
    DIVULGADOR  // Minhas publicações enviadas
}

data class FeedUiState(
    val userLocation: Coordinates? = null,
    val userProfile: Profile? = null,
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedTags: List<String> = emptyList(),
    val raioMetros: Double = 10000.0, // Default 10km
    val activeMode: FeedMode = FeedMode.RECEBEDOR,
    val postToReport: Post? = null,
    val showCreateSheet: Boolean = false,
    val hasLocationPermission: Boolean = false
)
