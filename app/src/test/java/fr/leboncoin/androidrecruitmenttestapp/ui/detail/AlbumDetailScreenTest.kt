package fr.leboncoin.androidrecruitmenttestapp.ui.detail

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.adevinta.spark.SparkTheme
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags
import fr.leboncoin.domain.model.Album
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlbumDetailScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testAlbum = Album(
        id = 42,
        albumId = 1,
        title = "Test Album Title",
        url = "https://example.com/img/42",
        thumbnailUrl = "https://example.com/thumb/42",
        isFavorite = false,
    )

    @Test
    fun loadingState_displaysLoadingIndicator() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreen(
                    uiState = AlbumDetailUiState.Loading,
                    onFavoriteToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertIsDisplayed()
    }

    @Test
    fun errorState_displaysErrorMessage() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreen(
                    uiState = AlbumDetailUiState.Error("Album not found"),
                    onFavoriteToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Album not found").assertIsDisplayed()
    }

    @Test
    fun successState_displaysAlbumTitle() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreen(
                    uiState = AlbumDetailUiState.Success(testAlbum),
                    onFavoriteToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Test Album Title").assertIsDisplayed()
    }

    @Test
    fun successState_displaysAlbumIdAndTrackIdChips() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreen(
                    uiState = AlbumDetailUiState.Success(testAlbum),
                    onFavoriteToggle = {},
                )
            }
        }

        // R.string.album_id_label = "Album #%1$d", albumId=1 → "Album #1"
        composeTestRule.onNodeWithText("Album #1").assertIsDisplayed()
        // R.string.track_id_label = "Track #%1$d", id=42 → "Track #42"
        composeTestRule.onNodeWithText("Track #42").assertIsDisplayed()
    }

    @Test
    fun successState_whenNotFavorite_showsAddToFavoritesButton() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreen(
                    uiState = AlbumDetailUiState.Success(testAlbum.copy(isFavorite = false)),
                    onFavoriteToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add to favorites").assertIsDisplayed()
    }

    @Test
    fun successState_whenFavorite_showsRemoveFromFavoritesButton() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreen(
                    uiState = AlbumDetailUiState.Success(testAlbum.copy(isFavorite = true)),
                    onFavoriteToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()
    }

    @Test
    fun successState_clickFavoriteButton_invokesOnFavoriteToggle() {
        var toggled = false

        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreen(
                    uiState = AlbumDetailUiState.Success(testAlbum),
                    onFavoriteToggle = { toggled = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add to favorites").performClick()

        assertTrue(toggled)
    }
}