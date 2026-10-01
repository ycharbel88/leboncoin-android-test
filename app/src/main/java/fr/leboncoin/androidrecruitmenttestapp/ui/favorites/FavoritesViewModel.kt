package fr.leboncoin.androidrecruitmenttestapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.androidrecruitmenttestapp.ui.state.AlbumsUiState
import fr.leboncoin.domain.usecase.GetFavoriteAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    getFavoriteAlbumsUseCase: GetFavoriteAlbumsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    val uiState: StateFlow<AlbumsUiState> = getFavoriteAlbumsUseCase()
        .map { albums ->
            if (albums.isEmpty()) {
                AlbumsUiState.Empty()
            } else {
                AlbumsUiState.Success(albums)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AlbumsUiState.Loading,
        ) // move to Flow Ext

    fun toggleFavorite(albumId: Int) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(albumId)
            } catch (_: Exception) {}
        }
    }
}
