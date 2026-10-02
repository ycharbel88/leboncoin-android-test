package fr.leboncoin.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import fr.leboncoin.data.local.AlbumDao
import fr.leboncoin.data.mapper.toDomain
import fr.leboncoin.data.mapper.toEntity
import fr.leboncoin.data.network.api.AlbumApiService
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlbumRepositoryImpl @Inject constructor(
    private val albumApiService: AlbumApiService,
    private val albumDao: AlbumDao,
) : AlbumRepository {

    override fun getAlbumsStream(): Flow<List<Album>> =
        albumDao.getAlbums().map { entities -> entities.map { it.toDomain() } }

    override fun getFavoriteAlbumsStream(): Flow<List<Album>> =
        albumDao.getFavoriteAlbums().map { entities -> entities.map { it.toDomain() } }

    override fun getAlbumByIdStream(id: Int): Flow<Album?> =
        albumDao.getAlbumById(id).map { it?.toDomain() }

    /**
     * NOTE: The static JSON endpoint downloads all 5 000 items in a single response.
     * Paging does NOT reduce this download; it only limits how many items are held in
     * memory / rendered at once.
     */
    override fun getAlbumsPaged(): Flow<PagingData<Album>> = Pager(
        config = PagingConfig(pageSize = 50, enablePlaceholders = false),
        pagingSourceFactory = { albumDao.getAlbumsPagingSource() },
    ).flow.map { pagingData ->
        pagingData.map { entity -> entity.toDomain() }
    }

    override suspend fun toggleFavorite(albumId: Int) {
        albumDao.toggleFavorite(albumId)
    }

    override suspend fun refreshAlbums() {
        val dtos = albumApiService.getAlbums()
        // Pass entities with isFavorite = false; refreshAtomically reads current favorites
        // and re-applies them atomically, so concurrent toggles are not lost.
        albumDao.refreshAtomically(dtos.map { dto -> dto.toEntity(isFavorite = false) })
    }
}