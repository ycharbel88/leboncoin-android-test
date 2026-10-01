package fr.leboncoin.data.repository

import app.cash.turbine.test
import fr.leboncoin.data.local.AlbumDao
import fr.leboncoin.data.local.AlbumEntity
import fr.leboncoin.data.network.api.AlbumApiService
import fr.leboncoin.data.network.model.AlbumDto
import fr.leboncoin.domain.model.Album
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import kotlin.coroutines.cancellation.CancellationException

class AlbumRepositoryImplTest {

    private val api = mock(AlbumApiService::class.java)
    private val dao = mock(AlbumDao::class.java)
    private val repository = AlbumRepositoryImpl(api, dao)

    @Test
    fun `albums stream maps database entities`() = runTest {
        `when`(dao.getAlbums()).thenReturn(
            flowOf(listOf(entity(1), entity(2, favorite = true)))
        )

        repository.getAlbumsStream().test {
            assertEquals(
                listOf(album(1), album(2, favorite = true)),
                awaitItem(),
            )
            awaitComplete()
        }
    }

    @Test
    fun `favorites stream maps favorite entities`() = runTest {
        `when`(dao.getFavoriteAlbums()).thenReturn(
            flowOf(listOf(entity(favorite = true)))
        )

        repository.getFavoriteAlbumsStream().test {
            assertEquals(listOf(album(favorite = true)), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `album by id stream maps album and handles removal`() = runTest {
        val source = MutableStateFlow<AlbumEntity?>(entity(42))
        `when`(dao.getAlbumById(42)).thenReturn(source)

        repository.getAlbumByIdStream(42).test {
            assertEquals(album(42), awaitItem())

            source.value = null
            assertNull(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        verify(dao).getAlbumById(42)
    }

    @Test
    fun `toggle favorite delegates requested id`() = runTest {
        repository.toggleFavorite(42)

        verify(dao).toggleFavorite(42)
    }

    @Test
    fun `get all refreshes preserving favorites then returns database albums`() =
        runTest {
            `when`(api.getAlbums())
                .thenReturn(listOf(dto(1), dto(2), dto(3)))
            `when`(dao.isFavorite(1)).thenReturn(true)
            `when`(dao.isFavorite(2)).thenReturn(false)
            `when`(dao.isFavorite(3)).thenReturn(null)

            // Distinct data verifies that the result comes from the database.
            `when`(dao.getAlbums()).thenReturn(
                flowOf(listOf(entity(4, favorite = true)))
            )

            val result = repository.getAllAlbums()

            assertEquals(listOf(album(4, favorite = true)), result)
            verify(api).getAlbums()
            verify(dao).upsertAlbums(
                listOf(
                    entity(1, favorite = true),
                    entity(2, favorite = false),
                    entity(3, favorite = false),
                )
            )
        }

    @Test
    fun `get all returns cache when refresh fails`() = runTest {
        `when`(api.getAlbums())
            .thenThrow(IllegalStateException("Network failed"))
        `when`(dao.getAlbums()).thenReturn(
            flowOf(listOf(entity(favorite = true)))
        )

        val result = repository.getAllAlbums()

        assertEquals(listOf(album(favorite = true)), result)
    }

    @Test
    fun `get all rethrows refresh failure when cache is empty`() = runTest {
        val failure = IllegalStateException("Network failed")
        `when`(api.getAlbums()).thenThrow(failure)
        `when`(dao.getAlbums()).thenReturn(flowOf(emptyList()))

        assertSameFailure(failure) {
            repository.getAllAlbums()
        }
    }

    @Test
    fun `get all propagates cancellation without reading cache`() = runTest {
        val cancellation = CancellationException("Refresh cancelled")
        `when`(api.getAlbums()).thenThrow(cancellation)

        assertSameFailure(cancellation) {
            repository.getAllAlbums()
        }

        verifyNoInteractions(dao)
    }

    // Fixtures use explicit values instead of production mappers.

    private fun dto(id: Int = 1) = AlbumDto(
        id = id,
        albumId = id + 100,
        title = "Album $id",
        url = "https://example.com/images/$id.jpg",
        thumbnailUrl = "https://example.com/thumbnails/$id.jpg",
    )

    private fun entity(
        id: Int = 1,
        favorite: Boolean = false,
    ) = AlbumEntity(
        id = id,
        albumId = id + 100,
        title = "Album $id",
        url = "https://example.com/images/$id.jpg",
        thumbnailUrl = "https://example.com/thumbnails/$id.jpg",
        isFavorite = favorite,
    )

    private fun album(
        id: Int = 1,
        favorite: Boolean = false,
    ) = Album(
        id = id,
        albumId = id + 100,
        title = "Album $id",
        url = "https://example.com/images/$id.jpg",
        thumbnailUrl = "https://example.com/thumbnails/$id.jpg",
        isFavorite = favorite,
    )

    private suspend fun assertSameFailure(
        expected: Throwable,
        block: suspend () -> Unit,
    ) {
        val actual = try {
            block()
            null
        } catch (error: Throwable) {
            error
        }

        if (actual == null) {
            fail("Expected ${expected::class.java.simpleName}")
        }
        assertSame(expected, actual)
    }
}