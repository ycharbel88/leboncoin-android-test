package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAlbumsPagingUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    getAlbumsPagingUseCase: GetAlbumsPagingUseCase,
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {


    // Cache Room-backed paging data for this ViewModel's lifetime.
    val albumsPagingFlow: Flow<PagingData<Album>> = getAlbumsPagingUseCase()
        .cachedIn(viewModelScope)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Keeps refresh errors visible alongside cached content until cleared or refresh succeeds.
    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError.asStateFlow()

    fun refresh() {
        if (_isRefreshing.value) return   // prevent overlapping refreshes
        viewModelScope.launch {
            _isRefreshing.value = true
            _refreshError.value = null
            try {
                refreshAlbumsUseCase()
                // Room PagingSource is automatically invalidated → new data flows through
            } catch (e: CancellationException) {
                throw e                   // propagate cancellation
            } catch (e: Exception) {
                _refreshError.value = e.message
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun retry() = refresh()

    fun toggleFavorite(albumId: Int) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(albumId)
            } catch (_: Exception) {
            }
        }
    }

    fun clearRefreshError() {
        _refreshError.value = null
    }
}