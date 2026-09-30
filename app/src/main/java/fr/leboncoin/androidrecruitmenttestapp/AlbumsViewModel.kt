package fr.leboncoin.androidrecruitmenttestapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import fr.leboncoin.domain.usecase.GetAlbumsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlbumsViewModel(
    private val getAlbumsUseCase: GetAlbumsUseCase,
) : ViewModel() {

    constructor(repository: AlbumRepository) : this(GetAlbumsUseCase(repository))

    private val _albums = MutableStateFlow<List<Album>>(
        value = emptyList()
    )
    val albums: StateFlow<List<Album>> = _albums

    fun loadAlbums() {
        viewModelScope.launch {
            try {
                _albums.emit(getAlbumsUseCase())
            } catch (_: Exception) { /* TODO: Handle errors */ }
        }
    }

    class Factory(
        private val repository: AlbumRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AlbumsViewModel(repository) as T
        }
    }
}