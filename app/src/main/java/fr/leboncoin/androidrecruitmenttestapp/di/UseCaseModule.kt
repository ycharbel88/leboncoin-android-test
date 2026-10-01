package fr.leboncoin.androidrecruitmenttestapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.leboncoin.domain.repository.AlbumRepository
import fr.leboncoin.domain.usecase.GetAlbumByIdUseCase
import fr.leboncoin.domain.usecase.GetAlbumsUseCase
import fr.leboncoin.domain.usecase.GetFavoriteAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetAlbumsUseCase(
        albumRepository: AlbumRepository,
    ): GetAlbumsUseCase = GetAlbumsUseCase(albumRepository)

    @Provides
    @Singleton
    fun provideToggleFavoriteUseCase(
        albumRepository: AlbumRepository,
    ): ToggleFavoriteUseCase = ToggleFavoriteUseCase(albumRepository)

    @Provides
    @Singleton
    fun provideGetFavoriteAlbumsUseCase(
        albumRepository: AlbumRepository,
    ): GetFavoriteAlbumsUseCase = GetFavoriteAlbumsUseCase(albumRepository)

    @Provides
    @Singleton
    fun provideGetAlbumByIdUseCase(
        albumRepository: AlbumRepository,
    ): GetAlbumByIdUseCase = GetAlbumByIdUseCase(albumRepository)
}