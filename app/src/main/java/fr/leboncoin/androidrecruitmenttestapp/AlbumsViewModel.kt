package fr.leboncoin.androidrecruitmenttestapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAlbumsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase,
) : ViewModel() {

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
}