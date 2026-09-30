package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository

class GetAlbumsUseCase(
    private val albumRepository: AlbumRepository,
) {
    suspend operator fun invoke(): List<Album> {
        return albumRepository.getAllAlbums()
    }
}