package fr.leboncoin.androidrecruitmenttestapp.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adevinta.spark.ExperimentalSparkApi
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.chips.ChipTinted
import com.adevinta.spark.components.progress.Spinner
import com.adevinta.spark.components.scaffold.Scaffold
import fr.leboncoin.androidrecruitmenttestapp.R
import fr.leboncoin.domain.model.Album


@OptIn(ExperimentalSparkApi::class)
@Composable
fun AlbumDetailScreen(
    uiState: AlbumDetailUiState,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when (uiState) {
                is AlbumDetailUiState.Loading -> Spinner(modifier = Modifier.testTag(TestTags.LOADING_INDICATOR))

                is AlbumDetailUiState.Error -> Text(
                    text = uiState.message,
                    style = SparkTheme.typography.body1,
                    modifier = Modifier.padding(16.dp),
                )

                is AlbumDetailUiState.Success -> AlbumDetailContent(
                    album = uiState.album,
                    onFavoriteToggle = onFavoriteToggle,
                )
            }
        }
    }
}

@Composable
private fun AlbumDetailContent(
    album: Album,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AlbumArtwork(
            album = album,
            onFavoriteToggle = onFavoriteToggle,
        )

        Text(
            text = album.title,
            style = SparkTheme.typography.headline1,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChipTinted(text = stringResource(R.string.album_id_label, album.albumId))
            ChipTinted(text = stringResource(R.string.track_id_label, album.id))
        }
    }
}

@Composable
private fun AlbumArtwork(
    album: Album,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
    ) {

        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            model = ImageRequest.Builder(LocalContext.current)
                .data(album.url)
                .crossfade(true)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = album.title,
            contentScale = ContentScale.Crop
        )

        FavoriteButton(
            isFavorite = album.isFavorite,
            onClick = onFavoriteToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
        )
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (isFavorite) {
                    Icons.Filled.Favorite
                } else {
                    Icons.Outlined.FavoriteBorder
                },
                contentDescription = if (isFavorite) {
                    "Remove from favorites"
                } else {
                    "Add to favorites"
                },
                tint = if (isFavorite) Color.Red else Color.DarkGray,
            )
        }
    }
}