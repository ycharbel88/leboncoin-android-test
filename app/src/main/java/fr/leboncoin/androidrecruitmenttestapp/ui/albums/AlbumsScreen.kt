package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import fr.leboncoin.androidrecruitmenttestapp.R
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags
import fr.leboncoin.androidrecruitmenttestapp.ui.components.AlbumItem
import fr.leboncoin.androidrecruitmenttestapp.ui.components.AlbumsEmpty
import fr.leboncoin.androidrecruitmenttestapp.ui.components.AlbumsError
import fr.leboncoin.androidrecruitmenttestapp.ui.components.AlbumsErrorBanner
import fr.leboncoin.androidrecruitmenttestapp.ui.components.AlbumsLayout
import fr.leboncoin.androidrecruitmenttestapp.ui.components.AlbumsLoading
import fr.leboncoin.domain.model.Album

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    pagingItems: LazyPagingItems<Album>,
    isRefreshing: Boolean,
    refreshError: String?,
    onItemSelected: (Album) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onFavoriteToggle: ((Int) -> Unit)?,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val loadStates = pagingItems.loadState
    val pagingError = (loadStates.source.refresh as? LoadState.Error)
        ?: (loadStates.refresh as? LoadState.Error)
    val isLoading = isRefreshing ||
        loadStates.source.refresh is LoadState.Loading ||
        loadStates.refresh is LoadState.Loading
    val isEmpty = pagingItems.itemCount == 0
    val errorMessage = refreshError ?: pagingError?.let {
        stringResource(R.string.albums_load_error)
    }
    val retry: () -> Unit = if (refreshError != null) {
        onRetry
    } else {
        { pagingItems.retry() }
    }

    AlbumsLayout(modifier) {
        when {
            isEmpty && errorMessage != null -> AlbumsError(errorMessage, retry)
            isEmpty && isLoading -> AlbumsLoading()
            isEmpty -> AlbumsEmpty(onRefresh)
            else -> PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (errorMessage != null) {
                        AlbumsErrorBanner(
                            message = errorMessage,
                            onRetry = retry,
                            onDismiss = if (refreshError != null) onDismissError else null,
                        )
                    }
                    PagedAlbumsList(
                        pagingItems = pagingItems,
                        onItemSelected = onItemSelected,
                        onFavoriteToggle = onFavoriteToggle,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun PagedAlbumsList(
    pagingItems: LazyPagingItems<Album>,
    onItemSelected: (Album) -> Unit,
    onFavoriteToggle: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val appendState = pagingItems.loadState.append

    LazyColumn(
        modifier = modifier.testTag(TestTags.ALBUMS_LIST),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.id },
            contentType = { "album" },
        ) { index ->
            val album = pagingItems[index]
            if (album != null) {
                AlbumItem(
                    album = album,
                    onItemSelected = onItemSelected,
                    onFavoriteToggle = onFavoriteToggle,
                )
            }
        }

        when (appendState) {
            is LoadState.Loading -> item(key = "append_loading") {
                AlbumsLoading()
            }
            is LoadState.Error -> item(key = "append_error") {
                AlbumsError(
                    message = stringResource(R.string.albums_load_error),
                    onRetry = { pagingItems.retry() },
                )
            }
            is LoadState.NotLoading -> Unit
        }
    }
}
