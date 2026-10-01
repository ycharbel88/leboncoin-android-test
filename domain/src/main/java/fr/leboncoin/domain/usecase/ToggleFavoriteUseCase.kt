package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.repository.AlbumRepository

class ToggleFavoriteUseCase(
    private val albumRepository: AlbumRepository,
) {
    suspend operator fun invoke(albumId: Int) {
        albumRepository.toggleFavorite(albumId)
    }
}