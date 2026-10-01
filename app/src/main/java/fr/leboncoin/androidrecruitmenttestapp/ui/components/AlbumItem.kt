package fr.leboncoin.androidrecruitmenttestapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adevinta.spark.ExperimentalSparkApi
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.card.Card
import com.adevinta.spark.components.chips.ChipTinted
import fr.leboncoin.androidrecruitmenttestapp.R
import fr.leboncoin.domain.model.Album

private val AlbumItemHeight = 120.dp

@OptIn(ExperimentalSparkApi::class)
@Composable
fun AlbumItem(
    album: Album,
    onItemSelected: (Album) -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteToggle: ((Int) -> Unit)? = null,
) {
    Card(
        onClick = { onItemSelected(album) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(AlbumItemHeight),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AlbumThumbnail(
                thumbnailUrl = album.thumbnailUrl,
                modifier = Modifier.size(AlbumItemHeight),
            )

            AlbumItemContent(
                album = album,
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
            )

            if (onFavoriteToggle != null) {
                AlbumFavoriteButton(
                    isFavorite = album.isFavorite,
                    onClick = { onFavoriteToggle(album.id) },
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun AlbumThumbnail(
    thumbnailUrl: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageRequest = remember(context, thumbnailUrl) {
        ImageRequest.Builder(context)
            .data(thumbnailUrl)
            .crossfade(true)
            .build()
    }

    AsyncImage(
        model = imageRequest,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}

@Composable
private fun AlbumItemContent(
    album: Album,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = album.title,
            style = SparkTheme.typography.caption,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.weight(1f))

        AlbumIdentifiers(
            albumId = album.albumId,
            trackId = album.id,
        )
    }
}

@OptIn(ExperimentalSparkApi::class)
@Composable
private fun AlbumIdentifiers(
    albumId: Int,
    trackId: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ChipTinted(
            text = stringResource(R.string.album_id_label, albumId),
        )

        ChipTinted(
            text = stringResource(R.string.track_id_label, trackId),
        )
    }
}

@Composable
private fun AlbumFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = if (isFavorite) {
                Icons.Filled.Favorite
            } else {
                Icons.Outlined.FavoriteBorder
            },
            contentDescription = stringResource(
                R.string.favorite_btn_content_description,
            ),
            tint = if (isFavorite) Color.Red else Color.Gray,
        )
    }
}