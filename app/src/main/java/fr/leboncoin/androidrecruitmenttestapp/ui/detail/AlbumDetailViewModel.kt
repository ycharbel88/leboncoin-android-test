package fr.leboncoin.androidrecruitmenttestapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.domain.usecase.GetAlbumByIdUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getAlbumByIdUseCase: GetAlbumByIdUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private var itemId = savedStateHandle.get<Int>("albumId") ?: -1

    // Cancels the previous album observation when the selection changes,
    // preventing updates from previously viewed albums from overwriting the current detail.
    private var observeJob: Job? = null

    private val _uiState = MutableStateFlow<AlbumDetailUiState>(AlbumDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun getDetailAlbum(albumId: Int) {
        itemId = albumId
        savedStateHandle["albumId"] = albumId
        observeAlbumDetails(albumId)
    }

    private fun observeAlbumDetails(albumId: Int) {
        observeJob?.cancel()
        _uiState.value = AlbumDetailUiState.Loading

        observeJob = viewModelScope.launch {
            try {
                getAlbumByIdUseCase(albumId).collect { album ->
                    _uiState.value = if (album != null) {
                        AlbumDetailUiState.Success(album)
                    } else {
                        AlbumDetailUiState.Error("Album not found or invalid ID: $albumId")
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value =
                    AlbumDetailUiState.Error(exception.message ?: "Unable to load album details")
            }
        }
    }

    fun toggleFavorite() {
        if (itemId == -1) return
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(itemId)
            } catch (_: Exception) {
            }
        }
    }
}