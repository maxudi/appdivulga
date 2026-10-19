package com.example.appdivulga.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdivulga.data.model.Post
import com.example.appdivulga.data.model.PostType
import com.example.appdivulga.data.repository.PostRepository
import com.example.appdivulga.location.Coordinates
import com.example.appdivulga.location.LocationManagerHelper
import com.example.appdivulga.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val locationManagerHelper: LocationManagerHelper,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    // ID do Usuário Ativo (UUID Válido)
    var currentUserId: String = "00000000-0000-0000-0000-000000000001"
        private set

    init {
        checkLocationPermissionAndFetch()
    }

    fun setCurrentUserId(id: String?) {
        if (!id.isNullOrEmpty()) {
            currentUserId = id
        }
    }

    fun checkLocationPermissionAndFetch() {
        val hasPermission = locationManagerHelper.hasLocationPermission()
        _uiState.update { it.copy(hasLocationPermission = hasPermission) }

        if (hasPermission) {
            viewModelScope.launch {
                val coords = locationManagerHelper.getCurrentLocation()
                if (coords != null) {
                    _uiState.update { it.copy(userLocation = coords) }
                    carregarOfertasProximas()
                } else {
                    val defaultCoords = Coordinates(-23.550520, -46.633308)
                    _uiState.update { it.copy(userLocation = defaultCoords) }
                    carregarOfertasProximas()
                }
            }
        } else {
            val defaultCoords = Coordinates(-23.550520, -46.633308)
            _uiState.update { it.copy(userLocation = defaultCoords) }
            carregarOfertasProximas()
        }
    }

    fun carregarOfertasProximas(isRefresh: Boolean = false) {
        val loc = _uiState.value.userLocation ?: return

        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            val tags = _uiState.value.selectedTags
            val raio = _uiState.value.raioMetros

            postRepository.buscarOfertasProximas(
                lat = loc.latitude,
                lng = loc.longitude,
                tags = tags,
                raioMetros = raio
            ).onSuccess { posts ->
                _uiState.update { state ->
                    state.copy(
                        posts = posts,
                        isLoading = false,
                        isRefreshing = false
                    )
                }

                val ofertaDestaque = posts.firstOrNull { it.tags.any { tag -> tags.contains(tag) } }
                if (ofertaDestaque != null) {
                    notificationHelper.showOfertaProximaNotification(
                        publicacaoId = ofertaDestaque.id,
                        titulo = ofertaDestaque.titulo,
                        descricao = ofertaDestaque.descricao ?: "Nova oferta encontrada nas redondezas!",
                        distanciaFormatada = ofertaDestaque.formatDistancia()
                    )
                }
            }.onFailure { err ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = err.message ?: "Erro ao carregar ofertas da região"
                    )
                }
            }
        }
    }

    fun curtirPublicacao(publicacaoId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val updatedList = state.posts.map { post ->
                    if (post.id == publicacaoId) {
                        val novoStatusCurtida = !post.curtidoPeloUsuario
                        val deltaCurtidas = if (novoStatusCurtida) 1 else -1
                        val deltaPontos = if (novoStatusCurtida) 10 else -10
                        post.copy(
                            curtidoPeloUsuario = novoStatusCurtida,
                            totalCurtidas = (post.totalCurtidas + deltaCurtidas).coerceAtLeast(0),
                            autorPontos = (post.autorPontos + deltaPontos).coerceAtLeast(0)
                        )
                    } else post
                }
                state.copy(posts = updatedList)
            }

            postRepository.curtirPublicacao(publicacaoId, currentUserId)
                .onFailure {
                    carregarOfertasProximas()
                }
        }
    }

    fun abrirDialogDenuncia(post: Post) {
        _uiState.update { it.copy(postToReport = post) }
    }

    fun fecharDialogDenuncia() {
        _uiState.update { it.copy(postToReport = null) }
    }

    fun denunciarPublicacao(publicacaoId: String, motivo: String, detalhes: String?) {
        viewModelScope.launch {
            fecharDialogDenuncia()

            _uiState.update { state ->
                val updated = state.posts.filter { it.id != publicacaoId }
                state.copy(posts = updated)
            }

            postRepository.denunciarPublicacao(
                publicacaoId = publicacaoId,
                denuncianteId = currentUserId,
                motivo = motivo,
                detalhes = detalhes
            )
        }
    }

    fun excluirPublicacao(publicacaoId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val updated = state.posts.filter { it.id != publicacaoId }
                state.copy(posts = updated)
            }
            postRepository.excluirPublicacao(publicacaoId)
        }
    }

    fun abrirSheetCriarPost() {
        _uiState.update { it.copy(showCreateSheet = true) }
    }

    fun fecharSheetCriarPost() {
        _uiState.update { it.copy(showCreateSheet = false) }
    }

    fun criarPublicacao(
        titulo: String,
        descricao: String?,
        midiaBytes: ByteArray?,
        fileName: String?,
        tipoMidia: PostType,
        linkExterno: String?,
        tags: List<String>
    ) {
        val loc = _uiState.value.userLocation ?: Coordinates(-23.550520, -46.633308)

        viewModelScope.launch {
            fecharSheetCriarPost()
            _uiState.update { it.copy(isLoading = true) }

            postRepository.criarPublicacao(
                autorId = currentUserId,
                titulo = titulo,
                descricao = descricao,
                midiaBytes = midiaBytes,
                fileName = fileName,
                tipoMidia = tipoMidia,
                linkExterno = linkExterno,
                tags = tags,
                lat = loc.latitude,
                lng = loc.longitude
            ).onSuccess {
                carregarOfertasProximas()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = err.message ?: "Erro ao criar publicação"
                    )
                }
            }
        }
    }

    fun toggleTagFilter(tag: String) {
        _uiState.update { state ->
            val currentTags = state.selectedTags.toMutableList()
            if (currentTags.contains(tag)) {
                currentTags.remove(tag)
            } else {
                currentTags.add(tag)
            }
            state.copy(selectedTags = currentTags)
        }
        carregarOfertasProximas()
    }

    fun setRaioMetros(raio: Double) {
        _uiState.update { it.copy(raioMetros = raio) }
        carregarOfertasProximas()
    }

    fun setFeedMode(mode: FeedMode) {
        _uiState.update { it.copy(activeMode = mode) }
    }
}
