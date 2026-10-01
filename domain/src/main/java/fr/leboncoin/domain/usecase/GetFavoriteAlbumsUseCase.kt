package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow

class GetFavoriteAlbumsUseCase(
    private val albumRepository: AlbumRepository,
) {
    operator fun invoke(): Flow<List<Album>> {
        return albumRepository.getFavoriteAlbumsStream()
    }
}