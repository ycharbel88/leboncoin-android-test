package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAlbumsUseCaseTest {

    @Test
    fun invoke_returnsAlbumsFromRepository() = runTest {
        val expected = listOf(
            Album(id = 1, albumId = 1, title = "Test Album", url = "https://url", thumbnailUrl = "https://thumb")
        )
        val fakeRepo = object : AlbumRepository {
            override suspend fun getAllAlbums(): List<Album> = expected
        }
        val useCase = GetAlbumsUseCase(fakeRepo)

        val actual = useCase()

        assertEquals(expected, actual)
    }
}