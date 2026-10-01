package fr.leboncoin.domain.usecase

import app.cash.turbine.test
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class GetFavoriteAlbumsUseCaseTest {

    private val repository = mock(AlbumRepository::class.java)
    private val useCase = GetFavoriteAlbumsUseCase(repository)

    @Test
    fun `invoke emits favorite albums from repository`() = runTest {
        val albums = listOf(mock(Album::class.java))
        `when`(repository.getFavoriteAlbumsStream())
            .thenReturn(flowOf(albums))

        useCase().test {
            assertEquals(albums, awaitItem())
            awaitComplete()
        }

        verify(repository).getFavoriteAlbumsStream()
    }

    @Test
    fun `invoke emits empty list when there are no favorites`() = runTest {
        `when`(repository.getFavoriteAlbumsStream())
            .thenReturn(flowOf(emptyList()))

        useCase().test {
            assertEquals(emptyList<Album>(), awaitItem())
            awaitComplete()
        }

        verify(repository).getFavoriteAlbumsStream()
    }
}