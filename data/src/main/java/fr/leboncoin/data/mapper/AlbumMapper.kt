package fr.leboncoin.data.mapper

import fr.leboncoin.data.local.AlbumEntity
import fr.leboncoin.data.network.model.AlbumDto
import fr.leboncoin.domain.model.Album

fun AlbumDto.toEntity(isFavorite: Boolean = false): AlbumEntity = AlbumEntity(
    id = id,
    albumId = albumId,
    title = title,
    url = url,
    thumbnailUrl = thumbnailUrl,
    isFavorite = isFavorite,
)

fun AlbumEntity.toDomain(): Album = Album(
    id = id,
    albumId = albumId,
    title = title,
    url = url,
    thumbnailUrl = thumbnailUrl,
    isFavorite = isFavorite,
)

fun AlbumDto.toDomain(isFavorite: Boolean = false): Album = Album(
    id = id,
    albumId = albumId,
    title = title,
    url = url,
    thumbnailUrl = thumbnailUrl,
    isFavorite = isFavorite,
)