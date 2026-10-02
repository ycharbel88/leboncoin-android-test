package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import com.adevinta.spark.SparkTheme
import fr.leboncoin.androidrecruitmenttestapp.FakeRepository
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAlbumsPagingUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlbumsScreenRouteTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testAlbum = Album(
        id = 42,
        albumId = 1,
        title = "Albums Route Test Album",
        url = "https://example.com/img/42",
        thumbnailUrl = "https://example.com/thumb/42",
        isFavorite = false,
    )

    private fun makeViewModel(
        albums: List<Album> = emptyList(),
        onRefresh: suspend () -> Unit = {},
        onToggle: suspend (Int) -> Unit = {},
    ): AlbumsViewModel {
        val fakeRepo = object : FakeRepository() {
            override fun getAlbumsPaged(): Flow<PagingData<Album>> =
                flowOf(if (albums.isEmpty()) PagingData.empty() else PagingData.from(albums))
            override suspend fun refreshAlbums() = onRefresh()
            override suspend fun toggleFavorite(albumId: Int) = onToggle(albumId)
        }
        return AlbumsViewModel(
            getAlbumsPagingUseCase = GetAlbumsPagingUseCase(fakeRepo),
            refreshAlbumsUseCase = RefreshAlbumsUseCase(fakeRepo),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(fakeRepo),
        )
    }

    @Test
    fun loadingState_displaysLoadingIndicator() {
        // refresh never returns → isRefreshing stays true; no paged items → Loading shown
        val viewModel = makeViewModel(
            albums = emptyList(),
            onRefresh = { suspendCancellableCoroutine { } },
        )

        composeTestRule.setContent {
            SparkTheme { AlbumsScreenRoute(onItemSelected = {}, viewModel = viewModel) }
        }

        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertIsDisplayed()
    }

    @Test
    fun successState_displaysAlbumList() {
        val viewModel = makeViewModel(albums = listOf(testAlbum))

        composeTestRule.setContent {
            SparkTheme { AlbumsScreenRoute(onItemSelected = {}, viewModel = viewModel) }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TestTags.ALBUMS_LIST).assertIsDisplayed()
        composeTestRule.onNodeWithText("Albums Route Test Album").assertIsDisplayed()
    }

    @Test
    fun errorState_displaysErrorMessageAndRetryButton() {
        val viewModel = makeViewModel(
            albums = emptyList(),
            onRefresh = { throw Exception("Network error") },
        )

        composeTestRule.setContent {
            SparkTheme { AlbumsScreenRoute(onItemSelected = {}, viewModel = viewModel) }
        }
        composeTestRule.waitForIdle()
        // With Robolectric the viewModelScope coroutine runs on the main looper;
        // poll until the error state node appears (max 5 s).
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(TestTags.ERROR_STATE).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(TestTags.ERROR_STATE).assertIsDisplayed()
        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
    }

    @Test
    fun successState_clickAlbumItem_callsOnItemSelected() {
        var selected: Album? = null
        val viewModel = makeViewModel(albums = listOf(testAlbum))

        composeTestRule.setContent {
            SparkTheme {
                AlbumsScreenRoute(
                    onItemSelected = { selected = it },
                    viewModel = viewModel,
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("${TestTags.ALBUM_ITEM}42").performClick()

        assertEquals(testAlbum, selected)
    }

    @Test
    fun successState_clickFavoriteButton_forwardsToggleToViewModel() {
        var toggledId: Int? = null
        val viewModel = makeViewModel(
            albums = listOf(testAlbum),
            onToggle = { toggledId = it },
        )

        composeTestRule.setContent {
            SparkTheme { AlbumsScreenRoute(onItemSelected = {}, viewModel = viewModel) }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("${TestTags.FAVORITE_BUTTON}42").performClick()
        composeTestRule.waitForIdle()

        assertEquals(42, toggledId)
    }

    @Test
    fun errorState_clickRetryButton_callsViewModelRetry() {
        var refreshCallCount = 0
        val viewModel = makeViewModel(
            albums = emptyList(),
            onRefresh = {
                refreshCallCount++
                throw Exception("Network error")
            },
        )

        composeTestRule.setContent {
            SparkTheme { AlbumsScreenRoute(onItemSelected = {}, viewModel = viewModel) }
        }
        composeTestRule.waitForIdle()
        // Wait until the retry button appears (error state rendered)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(TestTags.RETRY_BUTTON).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag(TestTags.RETRY_BUTTON).performClick()
        // Wait until retry refresh fires (count becomes 2)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            refreshCallCount >= 2
        }
        composeTestRule.waitForIdle()

        assertEquals(2, refreshCallCount)
    }
}