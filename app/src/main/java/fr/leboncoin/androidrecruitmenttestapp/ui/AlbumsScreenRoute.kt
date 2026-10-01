package fr.leboncoin.androidrecruitmenttestapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.leboncoin.androidrecruitmenttestapp.AlbumsViewModel
import fr.leboncoin.domain.model.Album

@Composable
fun AlbumsScreenRoute(
    onItemSelected: (Album) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlbumsScreen(
        uiState = uiState,
        onItemSelected = onItemSelected,
        onFavoriteToggle = viewModel::toggleFavorite,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}