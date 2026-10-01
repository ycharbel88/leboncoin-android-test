package fr.leboncoin.androidrecruitmenttestapp.ui.state

import fr.leboncoin.domain.model.Album

sealed interface AlbumsUiState {

    data object Loading : AlbumsUiState

    data class Success(
        val albums: List<Album>,
        val isRefreshing: Boolean = false,
    ) : AlbumsUiState

    data class Empty(
        val isRefreshing: Boolean = false,
    ) : AlbumsUiState

    data class Error(
        val message: String,
        val albums: List<Album> = emptyList(),
    ) : AlbumsUiState
}