package fr.leboncoin.domain.repository

import fr.leboncoin.domain.model.Album
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun getAlbumsStream(): Flow<List<Album>>
    fun getFavoriteAlbumsStream(): Flow<List<Album>>
    fun getAlbumByIdStream(id: Int): Flow<Album?>
    suspend fun toggleFavorite(albumId: Int)
    suspend fun refreshAlbums()
    suspend fun getAllAlbums(): List<Album>
}