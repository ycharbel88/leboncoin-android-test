package fr.leboncoin.androidrecruitmenttestapp

import app.cash.turbine.test
import fr.leboncoin.androidrecruitmenttestapp.ui.favorites.FavoritesViewModel
import fr.leboncoin.androidrecruitmenttestapp.ui.state.AlbumsUiState
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetFavoriteAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ── fixtures ─────────────────────────────────────────────────────────────────

    private val favAlbum = Album(
        id = 100, albumId = 1, title = "Fav Album",
        url = "u", thumbnailUrl = "tu", isFavorite = true,
    )
    private val favAlbum2 = favAlbum.copy(id = 200, title = "Fav Album 2")

    private val favoritesSubject = MutableStateFlow<List<Album>>(emptyList())

    private val getFavoriteAlbumsUseCase: GetFavoriteAlbumsUseCase = mock {
        on { invoke() } doReturn favoritesSubject
    }
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mock {
        on { invoke(any()) } doReturn Unit
    }

    private fun createViewModel() = FavoritesViewModel(
        getFavoriteAlbumsUseCase = getFavoriteAlbumsUseCase,
        toggleFavoriteUseCase = toggleFavoriteUseCase,
    )

    // ── Loading ──────────────────────────────────────────────────────────────────

    @Test
    fun `given stream never emits, when init, then state is Loading`() = runTest {
        // emptyFlow() never emits → stateIn keeps its Loading initialValue
        val neverUseCase: GetFavoriteAlbumsUseCase = mock {
            on { invoke() } doReturn emptyFlow()
        }
        val vm = FavoritesViewModel(neverUseCase, toggleFavoriteUseCase)

        vm.uiState.test {
            assertEquals(AlbumsUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Empty ────────────────────────────────────────────────────────────────────

    @Test
    fun `given stream emits empty list, when init, then state is Empty`() = runTest {
        // favoritesSubject starts with emptyList() → maps directly to Empty
        val vm = createViewModel()

        vm.uiState.test {
            assertTrue(awaitNonLoading() is AlbumsUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given Success state, when all favorites removed from stream, then transitions to Empty`() = runTest {
        // Given
        favoritesSubject.value = listOf(favAlbum)
        val vm = createViewModel()

        vm.uiState.test {
            awaitNonLoading() // consume initial Success

            // When
            favoritesSubject.value = emptyList()

            // Then
            assertTrue(awaitItem() is AlbumsUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Success ──────────────────────────────────────────────────────────────────

    @Test
    fun `given stream emits favorites, when init, then state is Success with those albums`() = runTest {
        // Given
        val favorites = listOf(favAlbum, favAlbum2)
        favoritesSubject.value = favorites
        val vm = createViewModel()

        vm.uiState.test {
            assertEquals(AlbumsUiState.Success(favorites), awaitNonLoading())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given Empty state, when stream emits a favorite, then transitions to Success`() = runTest {
        // Given – stream starts empty
        val vm = createViewModel()

        vm.uiState.test {
            awaitNonLoading() // consume initial Empty

            // When
            favoritesSubject.value = listOf(favAlbum)

            // Then
            assertEquals(AlbumsUiState.Success(listOf(favAlbum)), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given Success state, when stream adds another favorite, then emits updated Success`() = runTest {
        // Given
        favoritesSubject.value = listOf(favAlbum)
        val vm = createViewModel()

        vm.uiState.test {
            awaitNonLoading() // consume initial Success(favAlbum)

            // When
            favoritesSubject.value = listOf(favAlbum, favAlbum2)

            // Then
            val result = awaitItem() as AlbumsUiState.Success
            assertEquals(listOf(favAlbum, favAlbum2), result.albums)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Toggle favorite ──────────────────────────────────────────────────────────

    @Test
    fun `given any state, when toggleFavorite called, then ToggleFavoriteUseCase receives correct id`() = runTest {
        // Given
        favoritesSubject.value = listOf(favAlbum)
        val vm = createViewModel()
        advanceUntilIdle()

        // When
        vm.toggleFavorite(100)
        advanceUntilIdle()

        // Then
        verifyBlocking(toggleFavoriteUseCase) { invoke(100) }
    }

    // ── helper ───────────────────────────────────────────────────────────────────

    private suspend fun app.cash.turbine.TurbineTestContext<AlbumsUiState>.awaitNonLoading(): AlbumsUiState {
        var item = awaitItem()
        while (item is AlbumsUiState.Loading) item = awaitItem()
        return item
    }
}
