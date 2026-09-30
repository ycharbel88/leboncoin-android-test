package fr.leboncoin.androidrecruitmenttestapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.androidrecruitmenttestapp.ui.AlbumsUiState
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAlbumsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumsUiState>(AlbumsUiState.Loading)
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
        loadAlbums()
    }

    fun loadAlbums() {
        fetchAlbumsInternal(isUserRefresh = false)
    }

    fun refresh() {
        fetchAlbumsInternal(isUserRefresh = true)
    }

    fun retry() {
        fetchAlbumsInternal(isUserRefresh = false)
    }

    private fun fetchAlbumsInternal(isUserRefresh: Boolean) {
        if (fetchJob?.isActive == true) return

        val currentAlbums = getCurrentAlbums()

        if (isUserRefresh && currentAlbums.isNotEmpty()) {
            _uiState.value = AlbumsUiState.Success(albums = currentAlbums, isRefreshing = true)
        } else if (currentAlbums.isEmpty() && _uiState.value !is AlbumsUiState.Loading) {
            _uiState.value = AlbumsUiState.Loading
        }

        fetchJob = viewModelScope.launch {
            try {
                val albums = getAlbumsUseCase()
                if (albums.isEmpty()) {
                    _uiState.value = AlbumsUiState.Empty(isRefreshing = false)
                } else {
                    _uiState.value = AlbumsUiState.Success(albums = albums, isRefreshing = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (currentAlbums.isNotEmpty()) {
                    _uiState.value = AlbumsUiState.Error(
                        message = e.message ?: "An unexpected error occurred",
                        albums = currentAlbums,
                    )
                } else {
                    _uiState.value = AlbumsUiState.Error(
                        message = e.message ?: "An unexpected error occurred",
                    )
                }
            }
        }
    }

    private fun getCurrentAlbums(): List<Album> {
        return when (val state = _uiState.value) {
            is AlbumsUiState.Success -> state.albums
            is AlbumsUiState.Error -> state.albums
            else -> emptyList()
        }
    }
}
