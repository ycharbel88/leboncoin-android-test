package fr.leboncoin.domain.usecase

import app.cash.turbine.test
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class GetAlbumByIdUseCaseTest {

    private lateinit var repository: AlbumRepository
    private lateinit var useCase: GetAlbumByIdUseCase

    @Before
    fun setUp() {
        repository = mock(AlbumRepository::class.java)
        useCase = GetAlbumByIdUseCase(repository)
    }

    @Test
    fun `invoke emits album for requested id`() = runTest {
        val id = 42
        val album = mock(Album::class.java)
        `when`(repository.getAlbumByIdStream(id))
            .thenReturn(flowOf(album))

        useCase(id).test {
            assertEquals(album, awaitItem())
            awaitComplete()
        }

        verify(repository).getAlbumByIdStream(id)
    }

    @Test
    fun `invoke emits null when album does not exist`() = runTest {
        val id = 42
        `when`(repository.getAlbumByIdStream(id))
            .thenReturn(flowOf(null))

        useCase(id).test {
            assertNull(awaitItem())
            awaitComplete()
        }

        verify(repository).getAlbumByIdStream(id)
    }

    @Test
    fun `invoke forwards album updates`() = runTest {
        val id = 42
        val album = mock(Album::class.java)
        val updates = MutableStateFlow<Album?>(null)
        `when`(repository.getAlbumByIdStream(id))
            .thenReturn(updates)

        useCase(id).test {
            assertNull(awaitItem())

            updates.value = album
            assertEquals(album, awaitItem())

            updates.value = null
            assertNull(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        verify(repository).getAlbumByIdStream(id)
    }

    @Test
    fun `invoke propagates stream failure`() = runTest {
        val id = 42
        val exception = IllegalStateException("Unable to load album")
        `when`(repository.getAlbumByIdStream(id))
            .thenReturn(flow<Album?> { throw exception })

        useCase(id).test {
            assertSame(exception, awaitError())
        }

        verify(repository).getAlbumByIdStream(id)
    }
}