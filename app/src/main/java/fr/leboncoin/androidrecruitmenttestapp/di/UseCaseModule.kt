package fr.leboncoin.androidrecruitmenttestapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import fr.leboncoin.domain.repository.AlbumRepository
import fr.leboncoin.domain.usecase.GetAlbumByIdUseCase
import fr.leboncoin.domain.usecase.GetAlbumsPagingUseCase
import fr.leboncoin.domain.usecase.GetAlbumsStreamUseCase
import fr.leboncoin.domain.usecase.GetFavoriteAlbumsUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetAlbumsStreamUseCase(
        albumRepository: AlbumRepository,
    ): GetAlbumsStreamUseCase = GetAlbumsStreamUseCase(albumRepository)

    @Provides
    fun provideRefreshAlbumsUseCase(
        albumRepository: AlbumRepository,
    ): RefreshAlbumsUseCase = RefreshAlbumsUseCase(albumRepository)

    @Provides
    fun provideToggleFavoriteUseCase(
        albumRepository: AlbumRepository,
    ): ToggleFavoriteUseCase = ToggleFavoriteUseCase(albumRepository)

    @Provides
    fun provideGetFavoriteAlbumsUseCase(
        albumRepository: AlbumRepository,
    ): GetFavoriteAlbumsUseCase = GetFavoriteAlbumsUseCase(albumRepository)

    @Provides
    fun provideGetAlbumByIdUseCase(
        albumRepository: AlbumRepository,
    ): GetAlbumByIdUseCase = GetAlbumByIdUseCase(albumRepository)

    @Provides
    fun provideGetAlbumsPagingUseCase(
        albumRepository: AlbumRepository,
    ): GetAlbumsPagingUseCase = GetAlbumsPagingUseCase(albumRepository)
}