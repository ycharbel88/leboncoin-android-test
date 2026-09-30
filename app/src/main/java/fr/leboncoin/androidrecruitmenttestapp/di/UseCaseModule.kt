package fr.leboncoin.androidrecruitmenttestapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.leboncoin.domain.repository.AlbumRepository
import fr.leboncoin.domain.usecase.GetAlbumsUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetAlbumsUseCase(
        albumRepository: AlbumRepository,
    ): GetAlbumsUseCase = GetAlbumsUseCase(albumRepository)
}