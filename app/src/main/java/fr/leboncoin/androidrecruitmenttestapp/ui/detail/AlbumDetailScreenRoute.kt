package fr.leboncoin.androidrecruitmenttestapp.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AlbumDetailScreenRoute(
    albumId: Int? = null,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(albumId) {
        if (albumId != null && albumId != -1) {
            viewModel.getDetailAlbum(albumId)
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlbumDetailScreen(
        uiState = uiState,
        onFavoriteToggle = viewModel::toggleFavorite,
        modifier = modifier,
    )
}