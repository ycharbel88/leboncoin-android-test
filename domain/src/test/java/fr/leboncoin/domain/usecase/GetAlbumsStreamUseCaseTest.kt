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

class GetAlbumsStreamUseCaseTest {

    private val repository = mock(AlbumRepository::class.java)
    private val useCase = GetAlbumsStreamUseCase(repository)

    @Test
    fun `invoke emits albums from repository`() = runTest {
        val albums = listOf(mock(Album::class.java))
        `when`(repository.getAlbumsStream()).thenReturn(flowOf(albums))

        useCase().test {
            assertEquals(albums, awaitItem())
            awaitComplete()
        }

        verify(repository).getAlbumsStream()
    }

    @Test
    fun `invoke emits empty list when repository is empty`() = runTest {
        `when`(repository.getAlbumsStream())
            .thenReturn(flowOf(emptyList()))

        useCase().test {
            assertEquals(emptyList<Album>(), awaitItem())
            awaitComplete()
        }

        verify(repository).getAlbumsStream()
    }
}