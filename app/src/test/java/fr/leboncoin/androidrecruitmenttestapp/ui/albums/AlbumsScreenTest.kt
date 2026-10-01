package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
class AlbumsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loadingState_displaysLoadingIndicator() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumsScreen(
                    uiState = AlbumsUiState.Loading,
                    onItemSelected = {},
                    onRefresh = {},
                    onRetry = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertIsDisplayed()
    }

    @Test
    fun emptyState_displaysMessageAndCallsRefresh() {
        var refreshClicked = false

        composeTestRule.setContent {
            SparkTheme {
                AlbumsScreen(
                    uiState = AlbumsUiState.Empty(isRefreshing = false),
                    onItemSelected = {},
                    onRefresh = { refreshClicked = true },
                    onRetry = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.EMPTY_STATE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.REFRESH_BUTTON).performClick()

        assertTrue(refreshClicked)
    }

    @Test
    fun errorState_displaysErrorMessageAndCallsRetry() {
        var retryClicked = false

        composeTestRule.setContent {
            SparkTheme {
                AlbumsScreen(
                    uiState = AlbumsUiState.Error(message = "Connection timeout", albums = emptyList()),
                    onItemSelected = {},
                    onRefresh = {},
                    onRetry = { retryClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.ERROR_STATE).assertIsDisplayed()
        composeTestRule.onNodeWithText("Connection timeout").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.RETRY_BUTTON).performClick()

        assertTrue(retryClicked)
    }

    @Test
    fun successState_displaysAlbumsAndCallsOnItemSelected() {
        var selectedAlbum: Album? = null
        val testAlbum = Album(
            id = 42,
            albumId = 5,
            title = "Awesome Test Album Title",
            url = "https://url/42",
            thumbnailUrl = "https://thumb/42",
            isFavorite = false,
        )

        composeTestRule.setContent {
            SparkTheme {
                AlbumsScreen(
                    uiState = AlbumsUiState.Success(albums = listOf(testAlbum)),
                    onItemSelected = { selectedAlbum = it },
                    onRefresh = {},
                    onRetry = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.ALBUMS_LIST).assertIsDisplayed()
        composeTestRule.onNodeWithText("Awesome Test Album Title").assertIsDisplayed()

        composeTestRule.onNodeWithTag("${TestTags.ALBUM_ITEM}42").performClick()

        assertEquals(testAlbum, selectedAlbum)
    }

    @Test
    fun successState_togglingFavorite_callsOnFavoriteToggle() {
        var toggledId: Int? = null
        val testAlbum = Album(
            id = 99,
            albumId = 1,
            title = "Favorited Album Title",
            url = "https://url/99",
            thumbnailUrl = "https://thumb/99",
            isFavorite = false,
        )

        composeTestRule.setContent {
            SparkTheme {
                AlbumsScreen(
                    uiState = AlbumsUiState.Success(albums = listOf(testAlbum)),
                    onItemSelected = {},
                    onRefresh = {},
                    onRetry = {},
                    onFavoriteToggle = { toggledId = it },
                )
            }
        }

        composeTestRule.onNodeWithTag("${TestTags.FAVORITE_BUTTON}99").performClick()

        assertEquals(99, toggledId)
    }
}
