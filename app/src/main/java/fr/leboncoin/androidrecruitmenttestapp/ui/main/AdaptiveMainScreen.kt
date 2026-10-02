package fr.leboncoin.androidrecruitmenttestapp.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.window.core.layout.WindowSizeClass
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.scaffold.Scaffold
import fr.leboncoin.androidrecruitmenttestapp.R
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags
import fr.leboncoin.androidrecruitmenttestapp.ui.detail.AlbumDetailScreenRoute
import fr.leboncoin.androidrecruitmenttestapp.ui.albums.AlbumsScreenRoute
import fr.leboncoin.androidrecruitmenttestapp.ui.favorites.FavoritesScreenRoute
import fr.leboncoin.domain.model.Album
import kotlinx.coroutines.launch

private enum class AlbumTab(
    val label: String,
    val icon: ImageVector,
) {
    Albums(
        label = "Albums",
        icon = Icons.AutoMirrored.Filled.List,
    ),
    Favorites(
        label = "Favorites",
        icon = Icons.Default.Favorite,
    ),
}

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun AdaptiveMainScreen(
    modifier: Modifier = Modifier,
    albumsContent: @Composable (onItemSelected: (Album) -> Unit) -> Unit = { onItemSelected ->
        AlbumsScreenRoute(onItemSelected = onItemSelected)
    },
    favoritesContent: @Composable (onItemSelected: (Album) -> Unit) -> Unit = { onItemSelected ->
        FavoritesScreenRoute(onItemSelected = onItemSelected)
    },
    detailContent: @Composable (albumId: Int?) -> Unit = { albumId ->
        if (albumId != null) AlbumDetailScreenRoute(albumId = albumId)
        else AlbumDetailPlaceholder()
    },
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isCompact = !windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
    )
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    val coroutineScope = rememberCoroutineScope()
    val saveableStateHolder = rememberSaveableStateHolder()
    var selectedTab by rememberSaveable {
        mutableStateOf(AlbumTab.Albums)
    }

    val onAlbumSelected: (Album) -> Unit = { album ->
        coroutineScope.launch {
            navigator.navigateTo(
                pane = ListDetailPaneScaffoldRole.Detail,
                contentKey = album.id,
            )
        }
    }

    val onNavigateBack: () -> Unit = {
        coroutineScope.launch {
            navigator.navigateBack()
        }
    }

    val canNavigateBack = navigator.canNavigateBack()
    val isNotOnDefaultTab = selectedTab != AlbumTab.Albums

    BackHandler(
        enabled = canNavigateBack || isNotOnDefaultTab,
        onBack = {
            if (canNavigateBack) {
                onNavigateBack()
            } else {
                selectedTab = AlbumTab.Albums
            }
        },
    )

    val isDetailFullScreen = isCompact &&
            navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] ==
            PaneAdaptedValue.Expanded

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isDetailFullScreen) {
                            stringResource(R.string.title_album_details)
                        } else {
                            selectedTab.label
                        },
                        modifier = Modifier.testTag(TestTags.TOOLBAR_TITLE),
                    )
                },
                navigationIcon = {
                    if (isDetailFullScreen && canNavigateBack) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back_content_description),
                            )
                        }
                    }
                },
            )
        },

        bottomBar = {
            if (isCompact && !isDetailFullScreen) {
                AlbumNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier.testTag(TestTags.BOTTOM_NAV_BAR),
                )
            }
        },
    ) { paddingValues ->
        ListDetailPaneScaffold(

            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
            listPane = {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (!isCompact) {
                        AlbumNavigationBar(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Red)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopStart,
                    ) {
                        saveableStateHolder.SaveableStateProvider(key = selectedTab) {
                            when (selectedTab) {
                                AlbumTab.Albums -> albumsContent(onAlbumSelected)
                                AlbumTab.Favorites -> favoritesContent(onAlbumSelected)
                            }
                        }
                    }
                }
            },
            detailPane = {
                val selectedAlbumId = navigator.currentDestination?.contentKey
                detailContent(selectedAlbumId)
            },
        )
    }
}

@Composable
private fun AlbumNavigationBar(
    selectedTab: AlbumTab,
    onTabSelected: (AlbumTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        AlbumTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(text = tab.label)
                },
            )
        }
    }
}

@Composable
private fun AlbumDetailPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.select_album_to_view_details),
            style = SparkTheme.typography.body1,
        )
    }
}

@Preview
@Composable
private fun AlbumDetailPlaceholderPreview() {
    SparkTheme {
        AlbumDetailPlaceholder()
    }
}

@Preview
@Composable
private fun AlbumNavigationBarPreview() {
    SparkTheme {
        AlbumNavigationBar(
            selectedTab = AlbumTab.Albums,
            onTabSelected = {},
        )
    }
}

@Preview
@Composable
private fun AdaptiveMainScreenPreview() {
    SparkTheme {
        AdaptiveMainScreen(
            albumsContent = {},
            favoritesContent = {},
            detailContent = { AlbumDetailPlaceholder() },
        )
    }
}