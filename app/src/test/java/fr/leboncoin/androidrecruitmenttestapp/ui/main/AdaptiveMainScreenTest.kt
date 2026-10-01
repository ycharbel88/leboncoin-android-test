package fr.leboncoin.androidrecruitmenttestapp.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.adevinta.spark.SparkTheme
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags
import fr.leboncoin.domain.model.Album
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// ── fixtures shared across both layout classes ────────────────────────────────

private val albumA = Album(id = 42, albumId = 1, title = "Album A", url = "u", thumbnailUrl = "t")
private val albumB = Album(id = 99, albumId = 1, title = "Album B", url = "u", thumbnailUrl = "t")
private val favAlbum = Album(id = 77, albumId = 2, title = "Fav Album", url = "u", thumbnailUrl = "t")

/** Simple list pane: two tappable items that call onItemSelected. */
private fun albumsSlot(onItemSelected: (Album) -> Unit): @Composable () -> Unit = {
    Column {
        Text(
            text = albumA.title,
            modifier = Modifier
                .testTag("${TestTags.ALBUM_ITEM}${albumA.id}")
                .clickable { onItemSelected(albumA) },
        )
        Text(
            text = albumB.title,
            modifier = Modifier
                .testTag("${TestTags.ALBUM_ITEM}${albumB.id}")
                .clickable { onItemSelected(albumB) },
        )
    }
}

private fun favoritesSlot(onItemSelected: (Album) -> Unit): @Composable () -> Unit = {
    Text(
        text = favAlbum.title,
        modifier = Modifier
            .testTag("${TestTags.ALBUM_ITEM}${favAlbum.id}")
            .clickable { onItemSelected(favAlbum) },
    )
}

/** Detail pane: shows "Detail-<id>" when an album is selected, placeholder text otherwise. */
private val detailSlot: @Composable (Int?) -> Unit = { albumId ->
    if (albumId != null) {
        Text(text = "Detail-$albumId")
    } else {
        Text(text = "Select an album to view details")
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Compact layout tests  (default Robolectric display ~320 dp – compact)
// ─────────────────────────────────────────────────────────────────────────────

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AdaptiveMainScreenCompactTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun setContent() {
        composeTestRule.setContent {
            SparkTheme {
                AdaptiveMainScreen(
                    albumsContent = { onItemSelected -> albumsSlot(onItemSelected)() },
                    favoritesContent = { onItemSelected -> favoritesSlot(onItemSelected)() },
                    detailContent = detailSlot,
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    // ── Default state ─────────────────────────────────────────────────────────

    @Test
    fun compact_defaultState_albumsTitleShown_bottomNavVisible_noBackButton() {
        setContent()

        composeTestRule.onNodeWithTag(TestTags.TOOLBAR_TITLE).assertTextEquals("Albums")
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_BAR).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertDoesNotExist()
    }

    @Test
    fun compact_defaultState_albumsContentShown() {
        setContent()

        composeTestRule.onNodeWithText(albumA.title).assertIsDisplayed()
    }

    // ── Tab switching ─────────────────────────────────────────────────────────

    @Test
    fun compact_switchToFavorites_titleUpdatesFavoritesContentShown() {
        setContent()

        composeTestRule.onNodeWithText("Favorites").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TestTags.TOOLBAR_TITLE).assertTextEquals("Favorites")
        composeTestRule.onNodeWithText(favAlbum.title).assertIsDisplayed()
    }

    @Test
    fun compact_switchBackToAlbums_titleRestored() {
        setContent()

        composeTestRule.onNodeWithText("Favorites").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Albums").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TestTags.TOOLBAR_TITLE).assertTextEquals("Albums")
    }

    // ── Album selection → detail full-screen ─────────────────────────────────

    @Test
    fun compact_albumSelected_titleChangesToAlbumDetails() {
        setContent()

        composeTestRule.onNodeWithTag("${TestTags.ALBUM_ITEM}${albumA.id}").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TestTags.TOOLBAR_TITLE).assertTextEquals("Album details")
    }

    @Test
    fun compact_albumSelected_backButtonVisible_bottomNavHidden() {
        setContent()

        composeTestRule.onNodeWithTag("${TestTags.ALBUM_ITEM}${albumA.id}").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_BAR).assertDoesNotExist()
    }

    @Test
    fun compact_albumsTab_selection_passesCorrectIdToDetailSlot() {
        setContent()

        composeTestRule.onNodeWithTag("${TestTags.ALBUM_ITEM}${albumA.id}").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Detail-${albumA.id}").assertIsDisplayed()
    }

    @Test
    fun compact_favoritesTab_selection_passesCorrectIdToDetailSlot() {
        setContent()

        composeTestRule.onNodeWithText("Favorites").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("${TestTags.ALBUM_ITEM}${favAlbum.id}").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Detail-${favAlbum.id}").assertIsDisplayed()
    }

    // ── Back navigation ───────────────────────────────────────────────────────

    @Test
    fun compact_detailView_toolbarBackClick_restoresList_albumsTab() {
        setContent()

        composeTestRule.onNodeWithTag("${TestTags.ALBUM_ITEM}${albumA.id}").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TestTags.TOOLBAR_TITLE).assertTextEquals("Albums")
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_BAR).assertIsDisplayed()
    }

    @Test
    fun compact_detailView_systemBack_restoresAlbumsTab() {
        setContent()

        composeTestRule.onNodeWithTag("${TestTags.ALBUM_ITEM}${albumA.id}").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TestTags.TOOLBAR_TITLE).assertTextEquals("Albums")
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_BAR).assertIsDisplayed()
    }

    @Test
    fun compact_favoritesTab_detailView_systemBack_restoresFavoritesTab() {
        setContent()

        // Navigate to Favorites tab, select an album
        composeTestRule.onNodeWithText("Favorites").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("${TestTags.ALBUM_ITEM}${favAlbum.id}").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()

        // Should land back on the Favorites tab (selectedTab is preserved across navigation)
        composeTestRule.onNodeWithTag(TestTags.TOOLBAR_TITLE).assertTextEquals("Favorites")
    }

    // ── State restoration ─────────────────────────────────────────────────────

    @Test
    fun compact_tabState_favoritesSelected_restoredAfterRecreation() {
        val restorationTester = StateRestorationTester(composeTestRule)

        restorationTester.setContent {
            SparkTheme {
                AdaptiveMainScreen(
                    albumsContent = { onItemSelected -> albumsSlot(onItemSelected)() },
                    favoritesContent = { onItemSelected -> favoritesSlot(onItemSelected)() },
                    detailContent = detailSlot,
                )
            }
        }
        composeTestRule.waitForIdle()

        // Switch to Favorites tab
        composeTestRule.onNodeWithText("Favorites").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TestTags.TOOLBAR_TITLE).assertTextEquals("Favorites")

        // Simulate process death / config change and restore
        restorationTester.emulateSavedInstanceStateRestore()
        composeTestRule.waitForIdle()

        // rememberSaveable should restore the Favorites tab
        composeTestRule.onNodeWithTag(TestTags.TOOLBAR_TITLE).assertTextEquals("Favorites")
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Expanded layout tests  (w900dp → width ≥ 600 dp, two panes simultaneously)
// ─────────────────────────────────────────────────────────────────────────────

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w900dp")
class AdaptiveMainScreenExpandedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun setContent() {
        composeTestRule.setContent {
            SparkTheme {
                AdaptiveMainScreen(
                    albumsContent = { onItemSelected -> albumsSlot(onItemSelected)() },
                    favoritesContent = { onItemSelected -> favoritesSlot(onItemSelected)() },
                    detailContent = detailSlot,
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun expanded_initialState_placeholderVisibleInDetailPane() {
        setContent()

        composeTestRule.onNodeWithText("Select an album to view details").assertIsDisplayed()
    }

    @Test
    fun expanded_initialState_navBarInListPane_notAsBottomBar() {
        setContent()

        // In expanded mode the nav bar is inside the list pane column, not the bottomBar slot.
        // BOTTOM_NAV_BAR tag is only applied to the bottomBar slot → must not exist in expanded.
        composeTestRule.onNodeWithTag(TestTags.BOTTOM_NAV_BAR).assertDoesNotExist()
        // Tab navigation still works (clickable "Favorites" tab exists in the list pane nav bar).
        // assertIsDisplayed would be ambiguous here because "Albums" also appears in the toolbar;
        // verifying the click path is covered by the dedicated tab-switch test below.
    }

    @Test
    fun expanded_albumSelected_detailUpdates_listStillVisible() {
        setContent()

        composeTestRule.onNodeWithTag("${TestTags.ALBUM_ITEM}${albumA.id}").performClick()
        composeTestRule.waitForIdle()

        // Detail pane updated
        composeTestRule.onNodeWithText("Detail-${albumA.id}").assertIsDisplayed()
        // List pane still present (both panes visible simultaneously)
        composeTestRule.onNodeWithText(albumA.title).assertIsDisplayed()
    }

    @Test
    fun expanded_selectAnotherAlbum_detailUpdates() {
        setContent()

        composeTestRule.onNodeWithTag("${TestTags.ALBUM_ITEM}${albumA.id}").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Detail-${albumA.id}").assertIsDisplayed()

        composeTestRule.onNodeWithTag("${TestTags.ALBUM_ITEM}${albumB.id}").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Detail-${albumB.id}").assertIsDisplayed()
        assertFalse(
            "Detail-${albumA.id} should no longer be shown",
            composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasText("Detail-${albumA.id}")
            ).fetchSemanticsNodes().isNotEmpty(),
        )
    }

    @Test
    fun expanded_tabSwitch_toFavorites_favoritesContentShown() {
        setContent()

        composeTestRule.onNodeWithText("Favorites").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(favAlbum.title).assertIsDisplayed()
    }
}