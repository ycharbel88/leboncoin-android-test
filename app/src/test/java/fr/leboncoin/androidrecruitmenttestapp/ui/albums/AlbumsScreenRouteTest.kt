package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.adevinta.spark.SparkTheme
import fr.leboncoin.androidrecruitmenttestapp.FakeRepository
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAlbumsStreamUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
        albumsFlow: Flow<List<Album>> = emptyFlow(),
        onRefresh: suspend () -> Unit = {},
        onToggle: suspend (Int) -> Unit = {},
    ): AlbumsViewModel {
        val fakeRepo = object : FakeRepository() {
            override fun getAlbumsStream(): Flow<List<Album>> = albumsFlow
            override suspend fun refreshAlbums() = onRefresh()
            override suspend fun toggleFavorite(albumId: Int) = onToggle(albumId)
        }
        return AlbumsViewModel(
            getAlbumsStreamUseCase = GetAlbumsStreamUseCase(fakeRepo),
            refreshAlbumsUseCase = RefreshAlbumsUseCase(fakeRepo),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(fakeRepo),
        )
    }

    @Test
    fun loadingState_displaysLoadingIndicator() {
        // emptyFlow never emits → combine never produces a value → stateIn stays at Loading
        val viewModel = makeViewModel(albumsFlow = emptyFlow())

        composeTestRule.setContent {
            SparkTheme {
                AlbumsScreenRoute(
                    onItemSelected = {},
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertIsDisplayed()
    }

    @Test
    fun successState_displaysAlbumList() {
        val viewModel = makeViewModel(albumsFlow = MutableStateFlow(listOf(testAlbum)))

        composeTestRule.setContent {
            SparkTheme {
                AlbumsScreenRoute(
                    onItemSelected = {},
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TestTags.ALBUMS_LIST).assertIsDisplayed()
        composeTestRule.onNodeWithText("Albums Route Test Album").assertIsDisplayed()
    }

    @Test
    fun errorState_displaysErrorMessageAndRetryButton() {
        // Empty albums + refreshAlbums throws → Error state
        val viewModel = makeViewModel(
            albumsFlow = MutableStateFlow(emptyList()),
            onRefresh = { throw Exception("Network error") },
        )

        composeTestRule.setContent {
            SparkTheme {
                AlbumsScreenRoute(
                    onItemSelected = {},
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TestTags.ERROR_STATE).assertIsDisplayed()
        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
    }

    @Test
    fun successState_clickAlbumItem_callsOnItemSelected() {
        var selected: Album? = null
        val viewModel = makeViewModel(albumsFlow = MutableStateFlow(listOf(testAlbum)))

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
            albumsFlow = MutableStateFlow(listOf(testAlbum)),
            onToggle = { toggledId = it },
        )

        composeTestRule.setContent {
            SparkTheme {
                AlbumsScreenRoute(
                    onItemSelected = {},
                    viewModel = viewModel,
                )
            }
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
            albumsFlow = MutableStateFlow(emptyList()),
            onRefresh = {
                refreshCallCount++
                throw Exception("Network error")
            },
        )

        composeTestRule.setContent {
            SparkTheme {
                AlbumsScreenRoute(
                    onItemSelected = {},
                    viewModel = viewModel,
                )
            }
        }

        // init triggers one load; after waitForIdle the Error state is shown
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TestTags.RETRY_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // init call + retry click = 2 total refreshAlbums invocations
        assertEquals(2, refreshCallCount)
    }
}