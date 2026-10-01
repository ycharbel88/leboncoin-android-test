package fr.leboncoin.androidrecruitmenttestapp

import app.cash.turbine.test
import fr.leboncoin.androidrecruitmenttestapp.ui.albums.AlbumsViewModel
import fr.leboncoin.androidrecruitmenttestapp.ui.state.AlbumsUiState
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAlbumsStreamUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ── fixtures ─────────────────────────────────────────────────────────────────

    private val albumA = Album(id = 1, albumId = 10, title = "Album A", url = "u1", thumbnailUrl = "t1")
    private val albumB = Album(id = 2, albumId = 10, title = "Album B", url = "u2", thumbnailUrl = "t2")

    /**
     * Shared flow backing the stream use-case stub. Tests mutate its value before
     * creating the ViewModel so the combine sees the right initial data.
     */
    private val albumsSubject = MutableStateFlow<List<Album>>(emptyList())

    /**
     * Captured by refreshAlbumsUseCase's doAnswer. Change it inside a test body
     * to alter behaviour without re-stubbing (re-stubbing suspend fns is not
     * straightforward in mockito-kotlin without the mock{} DSL block).
     */
    private var refreshAction: () -> Unit = {}

    private val getAlbumsStreamUseCase: GetAlbumsStreamUseCase = mock {
        on { invoke() } doReturn albumsSubject
    }
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase = mock {
        on { invoke() } doAnswer { refreshAction() }
    }
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mock {
        on { invoke(any()) } doReturn Unit
    }

    private fun createViewModel() = AlbumsViewModel(
        getAlbumsStreamUseCase = getAlbumsStreamUseCase,
        refreshAlbumsUseCase = refreshAlbumsUseCase,
        toggleFavoriteUseCase = toggleFavoriteUseCase,
    )

    // ── Loading ──────────────────────────────────────────────────────────────────

    @Test
    fun `given stream never emits, when init, then state stays Loading`() = runTest {
        // albumsSubject is empty → combine never produces a value → stays at Loading
        val vm = createViewModel()

        vm.uiState.test {
            assertEquals(AlbumsUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Success ──────────────────────────────────────────────────────────────────

    @Test
    fun `given albums in stream, when init, then emits Success with those albums`() = runTest {
        // Given
        albumsSubject.value = listOf(albumA, albumB)
        val vm = createViewModel()

        vm.uiState.test {
            assertEquals(AlbumsUiState.Success(listOf(albumA, albumB)), awaitNonLoading())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given Success state, isRefreshing flag is false after init completes`() = runTest {
        albumsSubject.value = listOf(albumA)
        val vm = createViewModel()

        vm.uiState.test {
            val state = awaitNonLoading() as AlbumsUiState.Success
            assertEquals(false, state.isRefreshing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given Success state, when stream emits a new list, then emits updated Success`() = runTest {
        // Given
        albumsSubject.value = listOf(albumA)
        val vm = createViewModel()

        vm.uiState.test {
            val first = awaitNonLoading() as AlbumsUiState.Success
            assertEquals(listOf(albumA), first.albums)

            // When
            albumsSubject.value = listOf(albumA, albumB)

            // Then
            val second = awaitItem() as AlbumsUiState.Success
            assertEquals(listOf(albumA, albumB), second.albums)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Error ────────────────────────────────────────────────────────────────────

    @Test
    fun `given empty stream and refresh throws, when init, then emits Error with message`() = runTest {
        // albumsSubject is empty (default); refresh will throw
        refreshAction = { throw Exception("Network failure") }
        val vm = createViewModel()

        vm.uiState.test {
            val result = awaitNonLoading() as AlbumsUiState.Error
            assertEquals("Network failure", result.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given refresh throws with null message, when init, then error message uses fallback`() = runTest {
        refreshAction = { throw RuntimeException() }
        val vm = createViewModel()

        vm.uiState.test {
            val result = awaitNonLoading() as AlbumsUiState.Error
            assertEquals("Failed to refresh albums", result.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Retry / Refresh ──────────────────────────────────────────────────────────

    @Test
    fun `given Error state, when retry, then refreshAlbumsUseCase is called a second time`() = runTest {
        refreshAction = { throw Exception("error") }
        val vm = createViewModel()
        advanceUntilIdle()

        vm.retry()
        advanceUntilIdle()

        verifyBlocking(refreshAlbumsUseCase, times(2)) { invoke() }
    }

    @Test
    fun `given Success state, when refresh called, then refreshAlbumsUseCase is invoked again`() = runTest {
        albumsSubject.value = listOf(albumA)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.refresh()
        advanceUntilIdle()

        verifyBlocking(refreshAlbumsUseCase, times(2)) { invoke() }
    }

    // ── Toggle favorite ──────────────────────────────────────────────────────────

    @Test
    fun `given any state, when toggleFavorite called, then ToggleFavoriteUseCase receives correct id`() = runTest {
        albumsSubject.value = listOf(albumA)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.toggleFavorite(42)
        advanceUntilIdle()

        verifyBlocking(toggleFavoriteUseCase) { invoke(42) }
    }

    @Test
    fun `given multiple toggles, when each fires, then each id is forwarded independently`() = runTest {
        albumsSubject.value = listOf(albumA, albumB)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.toggleFavorite(1)
        vm.toggleFavorite(2)
        advanceUntilIdle()

        verifyBlocking(toggleFavoriteUseCase) { invoke(1) }
        verifyBlocking(toggleFavoriteUseCase) { invoke(2) }
    }

    // ── helper ───────────────────────────────────────────────────────────────────

    private suspend fun app.cash.turbine.TurbineTestContext<AlbumsUiState>.awaitNonLoading(): AlbumsUiState {
        var item = awaitItem()
        while (item is AlbumsUiState.Loading) item = awaitItem()
        return item
    }
}