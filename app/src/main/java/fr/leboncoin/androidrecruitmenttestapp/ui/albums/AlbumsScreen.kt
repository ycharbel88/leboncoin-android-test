package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.buttons.ButtonFilled
import com.adevinta.spark.components.progress.Spinner
import com.adevinta.spark.components.scaffold.Scaffold
import fr.leboncoin.androidrecruitmenttestapp.R
import fr.leboncoin.androidrecruitmenttestapp.ui.components.AlbumItem
import fr.leboncoin.androidrecruitmenttestapp.ui.state.AlbumsUiState
import fr.leboncoin.domain.model.Album

@Composable
fun AlbumsScreen(
    uiState: AlbumsUiState,
    onItemSelected: (Album) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteToggle: ((Int) -> Unit)? = null,
) {
    Scaffold(modifier = modifier) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when (uiState) {
                is AlbumsUiState.Loading -> Spinner()

                is AlbumsUiState.Empty -> {
                    AlbumsMessage(
                        message = "No albums found",
                        buttonText = stringResource(R.string.refresh_btn),
                        onClick = onRefresh,
                    )
                }

                is AlbumsUiState.Error -> {
                    if (uiState.albums.isEmpty()) {
                        AlbumsMessage(
                            message = uiState.message,
                            buttonText = stringResource(R.string.retry_btn),
                            onClick = onRetry,
                        )
                    } else {
                        AlbumsContent(
                            albums = uiState.albums,
                            isRefreshing = false,
                            errorMessage = uiState.message,
                            onItemSelected = onItemSelected,
                            onFavoriteToggle = onFavoriteToggle,
                        )
                    }
                }

                is AlbumsUiState.Success -> {
                    AlbumsContent(
                        albums = uiState.albums,
                        isRefreshing = uiState.isRefreshing,
                        errorMessage = null,
                        onItemSelected = onItemSelected,
                        onFavoriteToggle = onFavoriteToggle,
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumsContent(
    albums: List<Album>,
    isRefreshing: Boolean,
    errorMessage: String?,
    onItemSelected: (Album) -> Unit,
    onFavoriteToggle: ((Int) -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = SparkTheme.typography.body1,
                )
            }

            if (isRefreshing) {
                Spinner()
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                items = albums,
                key = { album -> album.id },
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

@Composable
private fun AlbumsMessage(
    message: String,
    buttonText: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = SparkTheme.typography.body1,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ButtonFilled(
            text = buttonText,
            onClick = onClick,
        )
    }
}