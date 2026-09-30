package fr.leboncoin.data.repository

import fr.leboncoin.data.mapper.toDomain
import fr.leboncoin.data.network.api.AlbumApiService
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository

class AlbumRepositoryImpl(
    private val albumApiService: AlbumApiService,
) : AlbumRepository {

    override suspend fun getAllAlbums(): List<Album> {
        return albumApiService.getAlbums().map { it.toDomain() }
    }
}