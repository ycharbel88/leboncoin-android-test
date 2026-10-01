package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.androidrecruitmenttestapp.ui.state.AlbumsUiState
import fr.leboncoin.domain.usecase.GetAlbumsStreamUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    getAlbumsStreamUseCase: GetAlbumsStreamUseCase,
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AlbumsUiState> = combine(
        getAlbumsStreamUseCase(),
        _isRefreshing,
        _errorMessage,
    ) { albums, isRefreshing, errorMsg ->
        when {
            albums.isNotEmpty() -> AlbumsUiState.Success(
                albums = albums,
                isRefreshing = isRefreshing,
            )
            errorMsg != null -> AlbumsUiState.Error(
                message = errorMsg,
                albums = emptyList(),
            )
            else -> AlbumsUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlbumsUiState.Loading,
    )

    init {
        loadAlbums()
    }

    fun loadAlbums() {
        refreshInternal()
    }

    fun refresh() {
        refreshInternal()
    }

    fun retry() {
        refreshInternal()
    }

    fun toggleFavorite(albumId: Int) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(albumId)
            } catch (_: Exception) {}
        }
    }

    private fun refreshInternal() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                _errorMessage.value = null
                refreshAlbumsUseCase()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to refresh albums"
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}