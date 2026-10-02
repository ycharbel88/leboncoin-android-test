package fr.leboncoin.androidrecruitmenttestapp.ui.favorites

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.adevinta.spark.SparkTheme
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags
import fr.leboncoin.androidrecruitmenttestapp.ui.state.AlbumsUiState
import fr.leboncoin.domain.model.Album
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FavoritesScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val albumA = Album(
        id = 10, albumId = 2, title = "Fav Album A",
        url = "u", thumbnailUrl = "t", isFavorite = true,
    )
    private val albumB = Album(
        id = 20, albumId = 3, title = "Fav Album B",
        url = "u", thumbnailUrl = "t", isFavorite = true,
    )

    private fun setScreen(
        uiState: AlbumsUiState,
        onItemSelected: (Album) -> Unit = {},
        onFavoriteToggle: ((Int) -> Unit)? = null,
        onRetry: () -> Unit = {},
        onRefresh: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            SparkTheme {
                FavoritesScreen(
                    uiState = uiState,
                    onItemSelected = onItemSelected,
                    onFavoriteToggle = onFavoriteToggle,
                    onRetry = onRetry,
                    onRefresh = onRefresh,
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    // ── Loading ───────────────────────────────────────────────────────────────

    @Test
    fun loadingState_displaysLoadingIndicator() {
        setScreen(uiState = AlbumsUiState.Loading)

        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertIsDisplayed()
    }

    // ── Empty ─────────────────────────────────────────────────────────────────

    @Test
    fun emptyState_displaysMessageAndRefreshButton() {
        var refreshCalled = false
        setScreen(
            uiState = AlbumsUiState.Empty(),
            onRefresh = { refreshCalled = true },
        )

        composeTestRule.onNodeWithTag(TestTags.EMPTY_STATE).assertIsDisplayed()
        composeTestRule.onNodeWithText("No albums found").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.REFRESH_BUTTON).performClick()
        assertTrue(refreshCalled)
    }

    // ── Success ───────────────────────────────────────────────────────────────

    @Test
    fun successState_displaysFavoriteAlbums() {
        setScreen(uiState = AlbumsUiState.Success(listOf(albumA, albumB)))

        composeTestRule.onNodeWithTag(TestTags.ALBUMS_LIST).assertIsDisplayed()
        composeTestRule.onNodeWithText("Fav Album A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fav Album B").assertIsDisplayed()
    }

    @Test
    fun successState_favoriteToggle_callsOnFavoriteToggle() {
        var toggledId: Int? = null
        setScreen(
            uiState = AlbumsUiState.Success(listOf(albumA)),
            onFavoriteToggle = { toggledId = it },
        )

        composeTestRule.onNodeWithTag("${TestTags.FAVORITE_BUTTON}10").performClick()
        assertEquals(10, toggledId)
    }

    // ── Error (full-screen, no cached albums) ─────────────────────────────────
    // FavoritesViewModel never emits Error today, but the screen handles it
    // in case error handling is wired in the future.

    @Test
    fun errorState_noAlbums_displaysFullScreenErrorAndRetryButton() {
        var retryCalled = false
        setScreen(
            uiState = AlbumsUiState.Error(message = "Failed to load favorites", albums = emptyList()),
            onRetry = { retryCalled = true },
        )

        composeTestRule.onNodeWithTag(TestTags.ERROR_STATE).assertIsDisplayed()
        composeTestRule.onNodeWithText("Failed to load favorites").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.RETRY_BUTTON).performClick()
        assertTrue(retryCalled)
    }

    // ── Error with stale albums (error banner above list) ─────────────────────

    @Test
    fun errorState_withCachedAlbums_displaysErrorBannerAboveList() {
        setScreen(
            uiState = AlbumsUiState.Error(message = "Partial failure", albums = listOf(albumA)),
        )

        // Stale list still visible
        composeTestRule.onNodeWithTag(TestTags.ALBUMS_LIST).assertIsDisplayed()
        composeTestRule.onNodeWithText("Fav Album A").assertIsDisplayed()
        // Error shown as banner, not as full-screen replacement
        composeTestRule.onNodeWithText("Partial failure").assertIsDisplayed()
    }
}