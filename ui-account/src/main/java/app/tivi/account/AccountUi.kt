/*
 * Copyright 2020 Google LLC
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

package app.tivi.account

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.tivi.common.compose.rememberStateWithLifecycle
import app.tivi.common.compose.theme.foregroundColor
import app.tivi.common.compose.ui.AsyncImage
import app.tivi.data.entities.TraktUser
import app.tivi.trakt.TraktAuthState
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

@Composable
fun AccountUi(
    openSettings: () -> Unit
) {
    AccountUi(
        viewModel = hiltViewModel(),
        openSettings = openSettings
    )
}

@Composable
internal fun AccountUi(
    viewModel: AccountUiViewModel,
    openSettings: () -> Unit
) {
    val viewState by rememberStateWithLifecycle(viewModel.state)

    val loginLauncher = rememberLauncherForActivityResult(
        viewModel.buildLoginActivityResult()
    ) { result ->
        if (result != null) {
            viewModel.onLoginResult(result)
        }
    }

    AccountUi(
        viewState = viewState,
        openSettings = openSettings,
        login = { loginLauncher.launch(Unit) },
        logout = { viewModel.logout() }
    )
}

@Composable
internal fun AccountUi(
    viewState: AccountUiViewState,
    openSettings: () -> Unit,
    login: () -> Unit,
    logout: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        elevation = 2.dp,
        // FIXME: Force the dialog to wrap the content. Need to work out why
        // this doesn't work automatically
        modifier = Modifier.heightIn(min = 200.dp)
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            if (viewState.user != null) {
                UserRow(
                    user = viewState.user,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            FlowRow(
                mainAxisAlignment = FlowMainAxisAlignment.End,
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 4.dp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .wrapContentSize(Alignment.CenterEnd)
                    .align(Alignment.End)
            ) {
                if (viewState.authState == TraktAuthState.LOGGED_OUT) {
                    OutlinedButton(onClick = login) {
                        Text(text = stringResource(R.string.login))
                    }
                } else {
                    TextButton(onClick = login) {
                        Text(text = stringResource(R.string.refresh_credentials))
                    }
                }

                OutlinedButton(onClick = logout) {
                    Text(text = stringResource(R.string.logout))
                }
            }

            Spacer(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth()
            )

            Divider()

            AppAction(
                label = stringResource(R.string.settings_title),
                icon = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings_title),
                onClick = openSettings
            )

            Spacer(
                modifier = Modifier
                    .height(8.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun UserRow(
    user: TraktUser,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        val avatarUrl = user.avatarUrl
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                requestBuilder = { crossfade(true) },
                contentDescription = stringResource(R.string.cd_profile_pic, user.name ?: user.username),
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = user.name ?: stringResource(R.string.account_name_unknown),
                style = MaterialTheme.typography.subtitle2
            )

            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

@Composable
private fun AppAction(
    label: String,
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Spacer(modifier = Modifier.width(8.dp))

        Image(
            imageVector = icon,
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(foregroundColor())
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.body2
        )
    }
}

@Preview
@Composable
fun PreviewUserRow() {
    UserRow(
        TraktUser(
            id = 0,
            username = "sammendes",
            name = "Sam Mendes",
            location = "London, UK",
            joined = OffsetDateTime.of(2019, 5, 4, 11, 12, 33, 0, ZoneOffset.UTC)
        )
    )
}
