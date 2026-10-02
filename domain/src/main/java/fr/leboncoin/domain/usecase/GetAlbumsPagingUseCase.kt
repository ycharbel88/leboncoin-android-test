package fr.leboncoin.domain.usecase

import androidx.paging.PagingData
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow

class GetAlbumsPagingUseCase(
    private val albumRepository: AlbumRepository,
) {
    operator fun invoke(): Flow<PagingData<Album>> = albumRepository.getAlbumsPaged()
}