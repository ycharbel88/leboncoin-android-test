package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class GetAlbumsUseCaseTest {

    private lateinit var repository: AlbumRepository
    private lateinit var useCase: GetAlbumsUseCase

    @Before
    fun setUp() {
        repository = mock(AlbumRepository::class.java)
        useCase = GetAlbumsUseCase(repository)
    }

    @Test
    fun `invoke returns albums from repository`() = runTest {
        val albums = listOf(mock(Album::class.java))
        `when`(repository.getAllAlbums()).thenReturn(albums)

        val result = useCase()

        assertEquals(albums, result)
        verify(repository).getAllAlbums()
    }

    @Test
    fun `invoke returns empty list when repository is empty`() = runTest {
        `when`(repository.getAllAlbums()).thenReturn(emptyList())

        val result = useCase()

        assertEquals(emptyList<Album>(), result)
        verify(repository).getAllAlbums()
    }

    @Test
    fun `invoke propagates repository exception`() = runTest {
        val exception = IllegalStateException("Unable to load albums")
        `when`(repository.getAllAlbums()).thenThrow(exception)

        try {
            useCase()
            fail("Expected repository exception")
        } catch (actual: IllegalStateException) {
            assertSame(exception, actual)
        }

        verify(repository).getAllAlbums()
    }
}