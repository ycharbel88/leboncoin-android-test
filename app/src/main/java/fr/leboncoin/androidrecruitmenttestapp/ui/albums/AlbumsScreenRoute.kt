package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import fr.leboncoin.domain.model.Album

@Composable
fun AlbumsScreenRoute(
    onItemSelected: (Album) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumsViewModel = hiltViewModel(),
) {
    val pagingItems = viewModel.albumsPagingFlow.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val refreshError by viewModel.refreshError.collectAsStateWithLifecycle()

    AlbumsScreen(
        pagingItems = pagingItems,
        isRefreshing = isRefreshing,
        refreshError = refreshError,
        onItemSelected = onItemSelected,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        onFavoriteToggle = viewModel::toggleFavorite,
        onDismissError = viewModel::clearRefreshError,
        modifier = modifier,
    )
}