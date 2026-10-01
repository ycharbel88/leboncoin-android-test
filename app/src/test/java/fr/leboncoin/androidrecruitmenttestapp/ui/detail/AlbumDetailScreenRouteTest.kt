package fr.leboncoin.androidrecruitmenttestapp.ui.detail

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import com.adevinta.spark.SparkTheme
import fr.leboncoin.androidrecruitmenttestapp.FakeRepository
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAlbumByIdUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlbumDetailScreenRouteTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testAlbum = Album(
        id = 42,
        albumId = 1,
        title = "Route Test Album",
        url = "https://example.com/img/42",
        thumbnailUrl = "https://example.com/thumb/42",
        isFavorite = false,
    )

    private fun makeViewModel(
        albumFlow: Flow<Album?>,
        onToggle: suspend (Int) -> Unit = {},
    ): AlbumDetailViewModel {
        val fakeRepo = object : FakeRepository() {
            override fun getAlbumByIdStream(id: Int): Flow<Album?> = albumFlow
            override suspend fun toggleFavorite(albumId: Int) = onToggle(albumId)
        }
        return AlbumDetailViewModel(
            savedStateHandle = SavedStateHandle(),
            getAlbumByIdUseCase = GetAlbumByIdUseCase(fakeRepo),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(fakeRepo),
        )
    }

    @Test
    fun withNullAlbumId_remainsInLoadingState() {
        val viewModel = makeViewModel(albumFlow = emptyFlow())

        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreenRoute(
                    albumId = null,
                    viewModel = viewModel,
                )
            }
        }

        // LaunchedEffect guards against null albumId — state stays at initial Loading
        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertIsDisplayed()
    }

    @Test
    fun withValidAlbumId_triggersLoadAndDisplaysAlbum() {
        val viewModel = makeViewModel(albumFlow = flowOf(testAlbum))

        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreenRoute(
                    albumId = 42,
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Route Test Album").assertIsDisplayed()
    }

    @Test
    fun withAlbumNotFound_displaysErrorState() {
        val viewModel = makeViewModel(albumFlow = flowOf(null))

        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreenRoute(
                    albumId = 999,
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Album not found or invalid ID: 999").assertIsDisplayed()
    }

    @Test
    fun favoriteButtonClick_forwardsToggleToViewModel() {
        var toggledId: Int? = null
        val viewModel = makeViewModel(
            albumFlow = flowOf(testAlbum),
            onToggle = { toggledId = it },
        )

        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreenRoute(
                    albumId = 42,
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Add to favorites").performClick()
        composeTestRule.waitForIdle()

        assertNotNull("toggleFavorite should have been called", toggledId)
    }
}