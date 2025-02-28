/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.common.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import app.tivi.common.compose.theme.AppBarAlphas
import app.tivi.common.compose.ui.PlaceholderPosterCard
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.RefreshButton
import app.tivi.common.compose.ui.SwipeDismissSnackbarHost
import app.tivi.common.compose.ui.plus
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <E : Entry> EntryGrid(
    lazyPagingItems: LazyPagingItems<out EntryWithShow<E>>,
    title: String,
    onNavigateUp: () -> Unit,
    onOpenShowDetails: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scaffoldState = rememberScaffoldState()

    lazyPagingItems.loadState.prependErrorOrNull()?.let { message ->
        LaunchedEffect(message) {
            scaffoldState.snackbarHostState.showSnackbar(message.message)
        }
    }
    lazyPagingItems.loadState.appendErrorOrNull()?.let { message ->
        LaunchedEffect(message) {
            scaffoldState.snackbarHostState.showSnackbar(message.message)
        }
    }
    lazyPagingItems.loadState.refreshErrorOrNull()?.let { message ->
        LaunchedEffect(message) {
            scaffoldState.snackbarHostState.showSnackbar(message.message)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            EntryGridAppBar(
                title = title,
                onNavigateUp = onNavigateUp,
                refreshing = lazyPagingItems.loadState.refresh == LoadState.Loading,
                onRefreshActionClick = { lazyPagingItems.refresh() },
                modifier = Modifier.fillMaxWidth()
            )
        },
        snackbarHost = { snackbarHostState ->
            SwipeDismissSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(horizontal = Layout.bodyMargin)
                    .fillMaxWidth()
            )
        },
        modifier = modifier
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(
                isRefreshing = lazyPagingItems.loadState.refresh == LoadState.Loading
            ),
            onRefresh = { lazyPagingItems.refresh() },
            indicatorPadding = paddingValues,
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    scale = true
                )
            }
        ) {
            val columns = Layout.columns
            val bodyMargin = Layout.bodyMargin
            val gutter = Layout.gutter

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns / 2),
                contentPadding = paddingValues +
                    PaddingValues(horizontal = bodyMargin, vertical = gutter),
                horizontalArrangement = Arrangement.spacedBy(gutter),
                verticalArrangement = Arrangement.spacedBy(gutter),
                modifier = Modifier
                    .bodyWidth()
                    .fillMaxHeight()
            ) {
                items(
                    items = lazyPagingItems,
                    key = { it.show.id }
                ) { entry ->
                    val mod = Modifier
                        .animateItemPlacement()
                        .aspectRatio(2 / 3f)
                        .fillMaxWidth()
                    if (entry != null) {
                        PosterCard(
                            show = entry.show,
                            poster = entry.poster,
                            onClick = { onOpenShowDetails(entry.show.id) },
                            modifier = mod
                        )
                    } else {
                        PlaceholderPosterCard(mod)
                    }
                }

                if (lazyPagingItems.loadState.append == LoadState.Loading) {
                    fullSpanItem {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryGridAppBar(
    title: String,
    refreshing: Boolean,
    onNavigateUp: () -> Unit,
    onRefreshActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.cd_navigate_up)
                )
            }
        },
        backgroundColor = MaterialTheme.colors.surface.copy(
            alpha = AppBarAlphas.translucentBarAlpha()
        ),
        contentColor = MaterialTheme.colors.onSurface,
        contentPadding = WindowInsets.statusBars.asPaddingValues(),
        modifier = modifier,
        title = { Text(text = title) },
        actions = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                // This button refresh allows screen-readers, etc to trigger a refresh.
                // We only show the button to trigger a refresh, not to indicate that
                // we're currently refreshing, otherwise we have 4 indicators showing the
                // same thing.
                Crossfade(
                    targetState = refreshing,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) { isRefreshing ->
                    if (!isRefreshing) {
                        RefreshButton(onClick = onRefreshActionClick)
                    }
                }
            }
        }
    )
}
