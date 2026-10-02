package fr.leboncoin.domain.repository

import androidx.paging.PagingData
import fr.leboncoin.domain.model.Album
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun getAlbumsStream(): Flow<List<Album>>
    fun getFavoriteAlbumsStream(): Flow<List<Album>>
    fun getAlbumByIdStream(id: Int): Flow<Album?>
    fun getAlbumsPaged(): Flow<PagingData<Album>>
    suspend fun toggleFavorite(albumId: Int)
    suspend fun refreshAlbums()
}