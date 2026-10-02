package fr.leboncoin.androidrecruitmenttestapp.ui.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.leboncoin.domain.model.Album

@Composable
fun FavoritesScreenRoute(
    onItemSelected: (Album) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritesScreen(
        uiState = uiState,
        onItemSelected = onItemSelected,
        onFavoriteToggle = viewModel::toggleFavorite,
        onRefresh = {},
        onRetry = {},
        modifier = modifier,
    )
}