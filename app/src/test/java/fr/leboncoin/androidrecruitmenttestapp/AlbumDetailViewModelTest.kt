package fr.leboncoin.androidrecruitmenttestapp

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import fr.leboncoin.androidrecruitmenttestapp.ui.detail.AlbumDetailUiState
import fr.leboncoin.androidrecruitmenttestapp.ui.detail.AlbumDetailViewModel
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAlbumByIdUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ── fixtures ─────────────────────────────────────────────────────────────────

    private val albumA = Album(
        id = 42, albumId = 1, title = "Detail Album",
        url = "https://url/42", thumbnailUrl = "https://thumb/42", isFavorite = false,
    )

    // getAlbumByIdUseCase.invoke() is NOT suspend, so `whenever` works directly.
    private val getAlbumByIdUseCase: GetAlbumByIdUseCase = mock()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mock {
        on { invoke(any()) } doReturn Unit
    }

    private fun createViewModel(savedAlbumId: Int = -1) = AlbumDetailViewModel(
        savedStateHandle = SavedStateHandle(
            if (savedAlbumId != -1) mapOf("albumId" to savedAlbumId) else emptyMap()
        ),
        getAlbumByIdUseCase = getAlbumByIdUseCase,
        toggleFavoriteUseCase = toggleFavoriteUseCase,
    )

    // ── Initial state ─────────────────────────────────────────────────────────────

    @Test
    fun `when created without calling getDetailAlbum, then state is Loading`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            assertEquals(AlbumDetailUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Success ──────────────────────────────────────────────────────────────────

    @Test
    fun `given album exists, when getDetailAlbum called, then emits Success`() = runTest {
        // Given
        whenever(getAlbumByIdUseCase(42)).thenReturn(flowOf(albumA))
        val vm = createViewModel()

        // When
        vm.getDetailAlbum(42)

        // Then
        vm.uiState.test {
            assertEquals(AlbumDetailUiState.Success(albumA), awaitNonLoading())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given live stream, when album updates in DB, then state reflects updated album`() = runTest {
        // Given
        val subject = MutableStateFlow<Album?>(albumA)
        whenever(getAlbumByIdUseCase(42)).thenReturn(subject)
        val vm = createViewModel()
        vm.getDetailAlbum(42)
        advanceUntilIdle()

        val updated = albumA.copy(isFavorite = true)

        vm.uiState.test {
            awaitNonLoading() // consume stale Success

            // When
            subject.value = updated

            // Then
            assertEquals(AlbumDetailUiState.Success(updated), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Error ────────────────────────────────────────────────────────────────────

    @Test
    fun `given album not found, when getDetailAlbum called, then emits Error containing id`() = runTest {
        // Given
        whenever(getAlbumByIdUseCase(999)).thenReturn(flowOf(null))
        val vm = createViewModel()

        // When
        vm.getDetailAlbum(999)

        // Then
        vm.uiState.test {
            val result = awaitNonLoading() as AlbumDetailUiState.Error
            assertTrue(result.message.contains("999"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given repository throws, when getDetailAlbum called, then emits Error with exception message`() = runTest {
        // Given
        whenever(getAlbumByIdUseCase(42)).thenReturn(
            flow { throw Exception("DB unavailable") }
        )
        val vm = createViewModel()

        // When
        vm.getDetailAlbum(42)

        // Then
        vm.uiState.test {
            assertEquals(AlbumDetailUiState.Error("DB unavailable"), awaitNonLoading())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given repository throws with null message, then error message uses fallback`() = runTest {
        // Given
        whenever(getAlbumByIdUseCase(42)).thenReturn(
            flow<Album?> { throw RuntimeException() }
        )
        val vm = createViewModel()
        vm.getDetailAlbum(42)

        vm.uiState.test {
            assertEquals(AlbumDetailUiState.Error("Unable to load album details"), awaitNonLoading())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Cancellation / multiple album IDs ────────────────────────────────────────

    @Test
    fun `given first album loading, when getDetailAlbum with new id, then state resets to Loading`() = runTest {
        // Given
        whenever(getAlbumByIdUseCase(42)).thenReturn(MutableStateFlow(albumA))
        whenever(getAlbumByIdUseCase(99)).thenReturn(emptyFlow()) // never resolves
        val vm = createViewModel()
        vm.getDetailAlbum(42)
        advanceUntilIdle()

        // When
        vm.getDetailAlbum(99)

        // Then – state resets because new stream never emits
        vm.uiState.test {
            assertEquals(AlbumDetailUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given hanging first observation, when second getDetailAlbum called, only second result shown`() = runTest {
        // Given
        val album99 = albumA.copy(id = 99)
        whenever(getAlbumByIdUseCase(42)).thenReturn(MutableStateFlow<Album?>(null)) // hanging / not found
        whenever(getAlbumByIdUseCase(99)).thenReturn(flowOf(album99))
        val vm = createViewModel()

        vm.getDetailAlbum(42)   // starts observation that returns null
        vm.getDetailAlbum(99)   // cancels previous and switches
        advanceUntilIdle()

        vm.uiState.test {
            assertEquals(AlbumDetailUiState.Success(album99), awaitNonLoading())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Toggle favorite ──────────────────────────────────────────────────────────

    @Test
    fun `given album loaded, when toggleFavorite, then ToggleFavoriteUseCase called with album id`() = runTest {
        // Given
        whenever(getAlbumByIdUseCase(42)).thenReturn(flowOf(albumA))
        val vm = createViewModel()
        vm.getDetailAlbum(42)
        advanceUntilIdle()

        // When
        vm.toggleFavorite()
        advanceUntilIdle()

        // Then
        verifyBlocking(toggleFavoriteUseCase) { invoke(42) }
    }

    @Test
    fun `given no album loaded (itemId minus one), when toggleFavorite, then use case is never called`() = runTest {
        // Given – ViewModel created without calling getDetailAlbum → itemId stays -1
        val vm = createViewModel()

        // When
        vm.toggleFavorite()
        advanceUntilIdle()

        // Then
        verifyBlocking(toggleFavoriteUseCase, never()) { invoke(any()) }
    }

    // ── SavedStateHandle ─────────────────────────────────────────────────────────

    @Test
    fun `given albumId in SavedStateHandle, when getDetailAlbum with same id, then loads that album`() = runTest {
        // Given – pre-populated SavedStateHandle (e.g. process-death restore)
        whenever(getAlbumByIdUseCase(42)).thenReturn(flowOf(albumA))
        val vm = createViewModel(savedAlbumId = 42)

        // When
        vm.getDetailAlbum(42)

        // Then
        vm.uiState.test {
            assertEquals(AlbumDetailUiState.Success(albumA), awaitNonLoading())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── helper ───────────────────────────────────────────────────────────────────

    private suspend fun app.cash.turbine.TurbineTestContext<AlbumDetailUiState>.awaitNonLoading(): AlbumDetailUiState {
        var item = awaitItem()
        while (item is AlbumDetailUiState.Loading) item = awaitItem()
        return item
    }
}
