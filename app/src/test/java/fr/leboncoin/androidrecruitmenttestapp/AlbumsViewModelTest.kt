package fr.leboncoin.androidrecruitmenttestapp

import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import fr.leboncoin.domain.usecase.GetAlbumsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `when albums are loaded then repository albums are emitted`() = runTest {
        // Given
        val expectedAlbums = listOf(
            Album(
                id = 1,
                albumId = 1,
                title = "My first album",
                url = "https://example.com/photo.jpg",
                thumbnailUrl = "https://example.com/thumbnail.jpg"
            )
        )

        val repository = object : AlbumRepository {
            override suspend fun getAllAlbums(): List<Album> = expectedAlbums
        }

        val viewModel = AlbumsViewModel(GetAlbumsUseCase(repository))

        // When
        viewModel.loadAlbums()

        // Then
        assertEquals(expectedAlbums, viewModel.albums.value)
    }
}