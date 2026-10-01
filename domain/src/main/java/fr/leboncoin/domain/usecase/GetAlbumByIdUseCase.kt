package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow

class GetAlbumByIdUseCase(
    private val albumRepository: AlbumRepository,
) {
    operator fun invoke(id: Int): Flow<Album?> {
        return albumRepository.getAlbumByIdStream(id)
    }
}