package fr.leboncoin.androidrecruitmenttestapp.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adevinta.spark.ExperimentalSparkApi
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.card.Card
import com.adevinta.spark.components.chips.ChipTinted
import fr.leboncoin.androidrecruitmenttestapp.R
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags
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
            .testTag("${TestTags.ALBUM_ITEM}${album.id}"),
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
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .testTag("${TestTags.FAVORITE_BUTTON}${album.id}"),
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = album.title,
            style = SparkTheme.typography.caption,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )


        AlbumIdentifiers(
            albumId = album.albumId,
            trackId = album.id,
        )
    }
}

@Composable
private fun AlbumIdentifiers(
    albumId: Int,
    trackId: Int,
    modifier: Modifier = Modifier,
) {
    val isLandscape =
        LocalConfiguration.current.orientation ==
                Configuration.ORIENTATION_LANDSCAPE

    val albumLabel = stringResource(R.string.album_id_label, albumId)
    val trackLabel = stringResource(R.string.track_id_label, trackId)

    if (isLandscape) {
        Column(
            modifier = modifier.width(IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ChipTinted(
                text = albumLabel,
                modifier = Modifier.fillMaxWidth(),
            )

            ChipTinted(
                text = trackLabel,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChipTinted(text = albumLabel)
            ChipTinted(text = trackLabel)
        }
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

@Preview
@Composable
private fun AlbumItemPreview() {
    SparkTheme {
        AlbumItem(
            album = Album(
                id = 1,
                albumId = 1,
                title = "accusamus quam diu et aut voluptatem",
                url = "https://via.placeholder.com/600/92c952",
                thumbnailUrl = "https://via.placeholder.com/150/92c952",
                isFavorite = true,
            ),
            onItemSelected = {},
            onFavoriteToggle = {},
        )
    }
}

@Preview
@Composable
private fun AlbumThumbnailPreview() {
    SparkTheme {
        AlbumThumbnail(
            thumbnailUrl = "https://via.placeholder.com/150/92c952",
            modifier = Modifier.size(120.dp),
        )
    }
}

@Preview
@Composable
private fun AlbumItemContentPreview() {
    SparkTheme {
        AlbumItemContent(
            album = Album(
                id = 1,
                albumId = 1,
                title = "accusamus quam diu et aut voluptatem",
                url = "https://via.placeholder.com/600/92c952",
                thumbnailUrl = "https://via.placeholder.com/150/92c952",
                isFavorite = true,
            ),
            modifier = Modifier.height(120.dp),
        )
    }
}

@Preview
@Composable
private fun AlbumIdentifiersPreview() {
    SparkTheme {
        AlbumIdentifiers(
            albumId = 1,
            trackId = 1,
        )
    }
}

@Preview
@Composable
private fun AlbumFavoriteButtonPreview() {
    SparkTheme {
        Row {
            AlbumFavoriteButton(
                isFavorite = true,
                onClick = {},
            )
            AlbumFavoriteButton(
                isFavorite = false,
                onClick = {},
            )
        }
    }
}