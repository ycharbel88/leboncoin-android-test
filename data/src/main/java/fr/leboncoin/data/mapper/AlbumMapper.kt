package fr.leboncoin.data.mapper

import fr.leboncoin.data.network.model.AlbumDto
import fr.leboncoin.domain.model.Album

fun AlbumDto.toDomain(isFavorite: Boolean = false): Album = Album(
    id = id,
    albumId = albumId,
    title = title,
    url = url,
    thumbnailUrl = thumbnailUrl,
    isFavorite = isFavorite,
)