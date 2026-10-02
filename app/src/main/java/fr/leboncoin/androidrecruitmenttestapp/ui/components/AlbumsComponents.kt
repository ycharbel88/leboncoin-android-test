package fr.leboncoin.androidrecruitmenttestapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.buttons.ButtonFilled
import com.adevinta.spark.components.progress.Spinner
import com.adevinta.spark.components.scaffold.Scaffold
import com.adevinta.spark.components.text.Text
import fr.leboncoin.androidrecruitmenttestapp.R
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags
import fr.leboncoin.androidrecruitmenttestapp.ui.TestTags.ALBUMS_REFRESH_RETRY

@Composable
internal fun AlbumsLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Scaffold(modifier = modifier) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
internal fun AlbumsLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Spinner(modifier = Modifier.testTag(TestTags.LOADING_INDICATOR))
    }
}

@Composable
internal fun AlbumsEmpty(onRefresh: () -> Unit) {
    AlbumsMessage(
        message = stringResource(R.string.albums_empty),
        buttonText = stringResource(R.string.refresh_btn),
        onClick = onRefresh,
        modifier = Modifier.testTag(TestTags.EMPTY_STATE),
        buttonTestTag = TestTags.REFRESH_BUTTON,
    )
}

@Composable
internal fun AlbumsError(message: String, onRetry: () -> Unit) {
    AlbumsMessage(
        message = message,
        buttonText = stringResource(R.string.retry_btn),
        onClick = onRetry,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTags.ERROR_STATE),
        buttonTestTag = TestTags.RETRY_BUTTON,
    )
}

@Composable
internal fun AlbumsErrorBanner(
    message: String,
    onRetry: () -> Unit,
    onDismiss: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = message, style = SparkTheme.typography.body1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ButtonFilled(
                text = stringResource(R.string.retry_btn),
                onClick = onRetry,
                modifier = Modifier
                    .weight(1f)
                    .testTag(ALBUMS_REFRESH_RETRY),
            )
            if (onDismiss != null) {
                ButtonFilled(
                    text = stringResource(R.string.albums_dismiss_error),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AlbumsMessage(
    message: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonTestTag: String? = null,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = message, style = SparkTheme.typography.body1)
        ButtonFilled(
            text = buttonText,
            onClick = onClick,
            modifier = buttonTestTag?.let { Modifier.testTag(it) } ?: Modifier,
        )
    }
}