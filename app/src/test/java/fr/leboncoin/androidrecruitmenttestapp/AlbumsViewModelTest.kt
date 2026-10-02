package fr.leboncoin.androidrecruitmenttestapp

import androidx.paging.PagingData
import fr.leboncoin.androidrecruitmenttestapp.ui.albums.AlbumsViewModel
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAlbumsPagingUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getAlbums: GetAlbumsPagingUseCase = mock {
        on { invoke() } doReturn flowOf(PagingData.empty<Album>())
    }
    private val refreshAlbums: RefreshAlbumsUseCase = mock()
    private val toggleFavorite: ToggleFavoriteUseCase = mock()

    private fun createViewModel() =
        AlbumsViewModel(getAlbums, refreshAlbums, toggleFavorite)

    @Test
    fun `init refreshes albums successfully`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        verify(refreshAlbums).invoke()
        assertFalse(vm.isRefreshing.value)
        assertNull(vm.refreshError.value)
    }

    @Test
    fun `failed refresh shows error and successful retry clears it`() = runTest {
        whenever(refreshAlbums.invoke())
            .thenThrow(IllegalStateException("Network failure"))
            .thenReturn(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("Network failure", vm.refreshError.value)
        assertFalse(vm.isRefreshing.value)

        vm.retry()
        advanceUntilIdle()

        verify(refreshAlbums, times(2)).invoke()
        assertNull(vm.refreshError.value)
        assertFalse(vm.isRefreshing.value)
    }

    @Test
    fun `clearRefreshError dismisses error`() = runTest {
        whenever(refreshAlbums.invoke())
            .thenThrow(IllegalStateException("Network failure"))

        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals("Network failure", vm.refreshError.value)

        vm.clearRefreshError()

        assertNull(vm.refreshError.value)
    }

    @Test
    fun `toggleFavorite forwards album id`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.toggleFavorite(42)
        advanceUntilIdle()

        verify(toggleFavorite).invoke(42)
    }
}