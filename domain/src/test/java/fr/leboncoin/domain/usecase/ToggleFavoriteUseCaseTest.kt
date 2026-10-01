package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class ToggleFavoriteUseCaseTest {

    private val repository = mock(AlbumRepository::class.java)
    private val useCase = ToggleFavoriteUseCase(repository)

    @Test
    fun `invoke toggles favorite for requested album id`() = runTest {
        val albumId = 42

        useCase(albumId)

        verify(repository).toggleFavorite(albumId)
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        val albumId = 42
        val exception = IllegalStateException("Unable to toggle favorite")
        doThrow(exception).`when`(repository).toggleFavorite(albumId)

        try {
            useCase(albumId)
            fail("Expected repository exception")
        } catch (actual: IllegalStateException) {
            assertSame(exception, actual)
        }

        verify(repository).toggleFavorite(albumId)
    }
}