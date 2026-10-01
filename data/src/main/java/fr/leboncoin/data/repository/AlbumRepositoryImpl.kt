package fr.leboncoin.data.repository

import fr.leboncoin.data.local.AlbumDao
import fr.leboncoin.data.mapper.toDomain
import fr.leboncoin.data.mapper.toEntity
import fr.leboncoin.data.network.api.AlbumApiService
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AlbumRepositoryImpl @Inject constructor(
    private val albumApiService: AlbumApiService,
    private val albumDao: AlbumDao,
) : AlbumRepository {

    override fun getAlbumsStream(): Flow<List<Album>> {
        return albumDao.getAlbums().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFavoriteAlbumsStream(): Flow<List<Album>> {
        return albumDao.getFavoriteAlbums().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAlbumByIdStream(id: Int): Flow<Album?> {
        return albumDao.getAlbumById(id).map { it?.toDomain() }
    }

    override suspend fun toggleFavorite(albumId: Int) {
        albumDao.toggleFavorite(albumId)
    }

    override suspend fun refreshAlbums() {
        val dtos = albumApiService.getAlbums()
        val entities = dtos.map { dto ->
            val existingFavorite = albumDao.isFavorite(dto.id) ?: false
            dto.toEntity(isFavorite = existingFavorite)
        }
        albumDao.upsertAlbums(entities)
    }

    override suspend fun getAllAlbums(): List<Album> {
        try {
            refreshAlbums()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val cached = getAlbumsStream().first()
            if (cached.isNotEmpty()) {
                return cached
            }
            throw e
        }
        return getAlbumsStream().first()
    }
}
