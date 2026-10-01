package fr.leboncoin.androidrecruitmenttestapp.ui.favorites

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
import fr.leboncoin.domain.usecase.GetFavoriteAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FavoritesScreenRouteTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testFavoriteAlbum = Album(
        id = 77,
        albumId = 3,
        title = "Favorites Route Test Album",
        url = "https://example.com/img/77",
        thumbnailUrl = "https://example.com/thumb/77",
        isFavorite = true,
    )

    private fun makeViewModel(
        favoritesFlow: Flow<List<Album>> = emptyFlow(),
        onToggle: suspend (Int) -> Unit = {},
    ): FavoritesViewModel {
        val fakeRepo = object : FakeRepository() {
            override fun getFavoriteAlbumsStream(): Flow<List<Album>> = favoritesFlow
            override suspend fun toggleFavorite(albumId: Int) = onToggle(albumId)
        }
        return FavoritesViewModel(
            getFavoriteAlbumsUseCase = GetFavoriteAlbumsUseCase(fakeRepo),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(fakeRepo),
        )
    }

    @Test
    fun loadingState_displaysLoadingIndicator() {
        // emptyFlow never emits → stateIn stays at initialValue Loading
        val viewModel = makeViewModel(favoritesFlow = emptyFlow())

        composeTestRule.setContent {
            SparkTheme {
                FavoritesScreenRoute(
                    onItemSelected = {},
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertIsDisplayed()
    }

    @Test
    fun emptyFavorites_displaysEmptyState() {
        val viewModel = makeViewModel(favoritesFlow = flowOf(emptyList()))

        composeTestRule.setContent {
            SparkTheme {
                FavoritesScreenRoute(
                    onItemSelected = {},
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TestTags.EMPTY_STATE).assertIsDisplayed()
        composeTestRule.onNodeWithText("No albums found").assertIsDisplayed()
    }

    @Test
    fun successState_displaysFavoriteAlbums() {
        val viewModel = makeViewModel(favoritesFlow = MutableStateFlow(listOf(testFavoriteAlbum)))

        composeTestRule.setContent {
            SparkTheme {
                FavoritesScreenRoute(
                    onItemSelected = {},
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TestTags.ALBUMS_LIST).assertIsDisplayed()
        composeTestRule.onNodeWithText("Favorites Route Test Album").assertIsDisplayed()
    }

    @Test
    fun successState_clickAlbumItem_callsOnItemSelected() {
        var selected: Album? = null
        val viewModel = makeViewModel(favoritesFlow = MutableStateFlow(listOf(testFavoriteAlbum)))

        composeTestRule.setContent {
            SparkTheme {
                FavoritesScreenRoute(
                    onItemSelected = { selected = it },
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("${TestTags.ALBUM_ITEM}77").performClick()

        assertEquals(testFavoriteAlbum, selected)
    }

    @Test
    fun successState_clickFavoriteButton_forwardsToggleToViewModel() {
        var toggledId: Int? = null
        val viewModel = makeViewModel(
            favoritesFlow = MutableStateFlow(listOf(testFavoriteAlbum)),
            onToggle = { toggledId = it },
        )

        composeTestRule.setContent {
            SparkTheme {
                FavoritesScreenRoute(
                    onItemSelected = {},
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("${TestTags.FAVORITE_BUTTON}77").performClick()
        composeTestRule.waitForIdle()

        assertEquals(77, toggledId)
    }
}