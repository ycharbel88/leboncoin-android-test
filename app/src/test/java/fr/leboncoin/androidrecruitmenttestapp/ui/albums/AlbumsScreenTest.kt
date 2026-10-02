package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.adevinta.spark.SparkTheme
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags
import fr.leboncoin.domain.model.Album
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlbumsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testAlbum = Album(
        id = 42, albumId = 5, title = "Direct Screen Test Album",
        url = "https://url/42", thumbnailUrl = "https://thumb/42",
    )

    // ── Loading ───────────────────────────────────────────────────────────────

    @Test
    fun loadingState_isRefreshing_withNoItems_displaysLoadingIndicator() {
        composeTestRule.setContent {
            SparkTheme {
                val pagingItems = flowOf(PagingData.empty<Album>()).collectAsLazyPagingItems()
                AlbumsScreen(
                    pagingItems = pagingItems,
                    isRefreshing = true,
                    refreshError = null,
                    onItemSelected = {},
                    onRefresh = {},
                    onRetry = {},
                    onFavoriteToggle = null,
                    onDismissError = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertIsDisplayed()
    }

    // ── Error (full-screen, no items) ─────────────────────────────────────────

    @Test
    fun errorState_noItems_withRefreshError_displaysErrorMessageAndRetryButton() {
        var retryCalled = false
        composeTestRule.setContent {
            SparkTheme {
                val pagingItems = flowOf(PagingData.empty<Album>()).collectAsLazyPagingItems()
                AlbumsScreen(
                    pagingItems = pagingItems,
                    isRefreshing = false,
                    refreshError = "Connection timeout",
                    onItemSelected = {},
                    onRefresh = {},
                    onRetry = { retryCalled = true },
                    onFavoriteToggle = null,
                    onDismissError = {},
                )
            }
        }
        // Paging settles asynchronously; poll until error state appears
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(TestTags.ERROR_STATE).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(TestTags.ERROR_STATE).assertIsDisplayed()
        composeTestRule.onNodeWithText("Connection timeout").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.RETRY_BUTTON).performClick()
        assertTrue(retryCalled)
    }

    // ── Success ───────────────────────────────────────────────────────────────

    @Test
    fun successState_favoriteToggle_callsOnFavoriteToggle() {
        var toggledId: Int? = null
        composeTestRule.setContent {
            SparkTheme {
                val pagingItems = flowOf(PagingData.from(listOf(testAlbum))).collectAsLazyPagingItems()
                AlbumsScreen(
                    pagingItems = pagingItems,
                    isRefreshing = false,
                    refreshError = null,
                    onItemSelected = {},
                    onRefresh = {},
                    onRetry = {},
                    onFavoriteToggle = { toggledId = it },
                    onDismissError = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("${TestTags.FAVORITE_BUTTON}42").performClick()
        assertEquals(42, toggledId)
    }

    // ── Error banner while list is shown ──────────────────────────────────────

    @Test
    fun successState_withRefreshError_showsErrorBannerAboveList() {
        composeTestRule.setContent {
            SparkTheme {
                val pagingItems = flowOf(PagingData.from(listOf(testAlbum))).collectAsLazyPagingItems()
                AlbumsScreen(
                    pagingItems = pagingItems,
                    isRefreshing = false,
                    refreshError = "Refresh failed",
                    onItemSelected = {},
                    onRefresh = {},
                    onRetry = {},
                    onFavoriteToggle = null,
                    onDismissError = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // Error banner message visible above the list
        composeTestRule.onNodeWithText("Refresh failed").assertIsDisplayed()
        // Album list is still shown beneath the banner
        composeTestRule.onNodeWithTag(TestTags.ALBUMS_LIST).assertIsDisplayed()
    }

    @Test
    fun successState_withRefreshError_dismissBannerCallsOnDismissError() {
        var dismissed = false
        composeTestRule.setContent {
            SparkTheme {
                val pagingItems = flowOf(PagingData.from(listOf(testAlbum))).collectAsLazyPagingItems()
                AlbumsScreen(
                    pagingItems = pagingItems,
                    isRefreshing = false,
                    refreshError = "Refresh failed",
                    onItemSelected = {},
                    onRefresh = {},
                    onRetry = {},
                    onFavoriteToggle = null,
                    onDismissError = { dismissed = true },
                )
            }
        }
        composeTestRule.waitForIdle()

        // The dismiss button appears in the error banner only when refreshError != null
        composeTestRule.onNodeWithText("Dismiss").performClick()
        assertTrue(dismissed)
    }
}
