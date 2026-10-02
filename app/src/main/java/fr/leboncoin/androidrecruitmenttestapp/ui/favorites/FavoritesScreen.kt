package fr.leboncoin.androidrecruitmenttestapp.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags
import fr.leboncoin.androidrecruitmenttestapp.ui.components.AlbumItem
import fr.leboncoin.androidrecruitmenttestapp.ui.components.AlbumsEmpty
import fr.leboncoin.androidrecruitmenttestapp.ui.components.AlbumsError
import fr.leboncoin.androidrecruitmenttestapp.ui.components.AlbumsErrorBanner
import fr.leboncoin.androidrecruitmenttestapp.ui.components.AlbumsLayout
import fr.leboncoin.androidrecruitmenttestapp.ui.components.AlbumsLoading
import fr.leboncoin.androidrecruitmenttestapp.ui.state.AlbumsUiState
import fr.leboncoin.domain.model.Album

@Composable
fun FavoritesScreen(
    uiState: AlbumsUiState,
    onItemSelected: (Album) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteToggle: ((Int) -> Unit)? = null,
) {
    AlbumsLayout(modifier) {
        when (uiState) {
            is AlbumsUiState.Loading -> AlbumsLoading()
            is AlbumsUiState.Empty -> AlbumsEmpty(onRefresh)
            is AlbumsUiState.Error -> {
                if (uiState.albums.isEmpty()) {
                    AlbumsError(uiState.message, onRetry)
                } else {
                    FavoritesContent(
                        albums = uiState.albums,
                        errorMessage = uiState.message,
                        onItemSelected = onItemSelected,
                        onFavoriteToggle = onFavoriteToggle,
                        onRetry = onRetry,
                    )
                }
            }

            is AlbumsUiState.Success -> FavoritesContent(
                albums = uiState.albums,
                errorMessage = null,
                onItemSelected = onItemSelected,
                onFavoriteToggle = onFavoriteToggle,
                onRetry = onRetry,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesContent(
    albums: List<Album>,
    errorMessage: String?,
    onItemSelected: (Album) -> Unit,
    onFavoriteToggle: ((Int) -> Unit)?,
    onRetry: () -> Unit,
) {

    Column(modifier = Modifier.fillMaxSize()) {
        if (errorMessage != null) {
            AlbumsErrorBanner(message = errorMessage, onRetry = onRetry)
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag(TestTags.ALBUMS_LIST),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                items = albums,
                key = { it.id },
                contentType = { "album" },
            ) { album ->
                AlbumItem(
                    album = album,
                    onItemSelected = onItemSelected,
                    onFavoriteToggle = onFavoriteToggle,
                )
            }
        }
    }
}