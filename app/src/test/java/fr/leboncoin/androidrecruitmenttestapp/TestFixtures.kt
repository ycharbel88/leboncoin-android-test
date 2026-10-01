package fr.leboncoin.androidrecruitmenttestapp

import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Minimal in-memory AlbumRepository for UI route tests that need a real ViewModel
 * constructed without Hilt. Override only the methods you need per-test.
 */
open class FakeRepository : AlbumRepository {
    override fun getAlbumsStream(): Flow<List<Album>> = emptyFlow()
    override fun getFavoriteAlbumsStream(): Flow<List<Album>> = emptyFlow()
    override fun getAlbumByIdStream(id: Int): Flow<Album?> = emptyFlow()
    override suspend fun toggleFavorite(albumId: Int) {}
    override suspend fun refreshAlbums() {}
    override suspend fun getAllAlbums(): List<Album> = emptyList()
}
