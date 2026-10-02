package fr.leboncoin.androidrecruitmenttestapp

import androidx.lifecycle.SavedStateHandle
import fr.leboncoin.androidrecruitmenttestapp.ui.detail.AlbumDetailUiState
import fr.leboncoin.androidrecruitmenttestapp.ui.detail.AlbumDetailViewModel
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAlbumByIdUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getAlbum: GetAlbumByIdUseCase = mock()
    private val toggleFavorite: ToggleFavoriteUseCase = mock()
    private val savedState = SavedStateHandle()

    private val album = Album(
        id = 42,
        albumId = 1,
        title = "Album",
        url = "https://example.com/image",
        thumbnailUrl = "https://example.com/thumbnail",
        isFavorite = false,
    )

    private fun createViewModel() =
        AlbumDetailViewModel(savedState, getAlbum, toggleFavorite)

    @Test
    fun `loads album and observes updates`() = runTest {
        val updates = MutableStateFlow<Album?>(album)
        whenever(getAlbum(42)).thenReturn(updates)
        val vm = createViewModel()
        assertEquals(AlbumDetailUiState.Loading, vm.uiState.value)

        vm.getDetailAlbum(42)
        advanceUntilIdle()

        assertEquals(AlbumDetailUiState.Success(album), vm.uiState.value)
        assertEquals(42, savedState.get<Int>("albumId"))

        val updated = album.copy(isFavorite = true)
        updates.value = updated
        advanceUntilIdle()

        assertEquals(AlbumDetailUiState.Success(updated), vm.uiState.value)
    }

    @Test
    fun `missing album shows error`() = runTest {
        whenever(getAlbum(42)).thenReturn(flowOf(null))
        val vm = createViewModel()

        vm.getDetailAlbum(42)
        advanceUntilIdle()

        assertEquals(
            AlbumDetailUiState.Error("Album not found or invalid ID: 42"),
            vm.uiState.value,
        )
    }

    @Test
    fun `observation failure shows error`() = runTest {
        whenever(getAlbum(42)).thenReturn(
            flow { throw IllegalStateException("DB unavailable") }
        )
        val vm = createViewModel()

        vm.getDetailAlbum(42)
        advanceUntilIdle()

        assertEquals(
            AlbumDetailUiState.Error("DB unavailable"),
            vm.uiState.value,
        )
    }

    @Test
    fun `switching albums stops previous observation`() = runTest {
        val previous = MutableStateFlow<Album?>(album)
        val next = album.copy(id = 99)
        whenever(getAlbum(42)).thenReturn(previous)
        whenever(getAlbum(99)).thenReturn(flowOf(next))
        val vm = createViewModel()

        vm.getDetailAlbum(42)
        advanceUntilIdle()
        assertEquals(AlbumDetailUiState.Success(album), vm.uiState.value)

        vm.getDetailAlbum(99)
        advanceUntilIdle()

        previous.value = album.copy(isFavorite = true)
        advanceUntilIdle()

        assertEquals(AlbumDetailUiState.Success(next), vm.uiState.value)
    }

    @Test
    fun `toggleFavorite uses selected album id`() = runTest {
        whenever(getAlbum(42)).thenReturn(flowOf(album))
        val vm = createViewModel()
        vm.getDetailAlbum(42)
        advanceUntilIdle()

        vm.toggleFavorite()
        advanceUntilIdle()

        verify(toggleFavorite).invoke(42)
    }

    @Test
    fun `toggleFavorite without selection does nothing`() = runTest {
        val vm = createViewModel()

        vm.toggleFavorite()
        advanceUntilIdle()

        verifyNoInteractions(toggleFavorite)
    }
}