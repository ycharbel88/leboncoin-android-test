package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class RefreshAlbumsUseCaseTest {

    private val repository = mock(AlbumRepository::class.java)
    private val useCase = RefreshAlbumsUseCase(repository)

    @Test
    fun `invoke refreshes albums in repository`() = runTest {
        useCase()

        verify(repository).refreshAlbums()
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        val exception = IllegalStateException("Unable to refresh albums")
        doThrow(exception).`when`(repository).refreshAlbums()

        try {
            useCase()
            fail("Expected repository exception")
        } catch (actual: IllegalStateException) {
            assertSame(exception, actual)
        }

        verify(repository).refreshAlbums()
    }
}