package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.repository.AlbumRepository

class RefreshAlbumsUseCase(
    private val albumRepository: AlbumRepository,
) {
    suspend operator fun invoke() {
        albumRepository.refreshAlbums()
    }
}
