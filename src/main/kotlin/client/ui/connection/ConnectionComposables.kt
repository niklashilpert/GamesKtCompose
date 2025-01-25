package client.ui.connection

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import client.connection.ConnectionInfo
import game.GameType
import kotlin.math.roundToInt

val POSITIVE_NUMBER = Regex("^\\d+$")

@Composable
internal fun ConnectionView(
    state: ConnectionUIState,
) {


    Row(
        modifier = Modifier
            .fillMaxWidth(.5f)
            .fillMaxHeight()
            .padding(16.dp)
    ) {


        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            ConnectionInputView(state.connectionInfo)
        }
        Divider(
            color = Color.Black,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxHeight()
                .weight(.003f)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            LobbyInputView(state)
        }
    }
}

@Composable
private fun ConnectionInputView(
    connectionInfo: ConnectionInfo
) {
    var gameTypeDropdownExpanded by remember { mutableStateOf(false) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(10),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .padding(20.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Connection",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
            )
        }
        item(span = { GridItemSpan(7) }) {
            OutlinedTextField(
                value = connectionInfo.ipAddress,
                onValueChange = { connectionInfo.ipAddress = it },
                label = { Text("IP address / Domain name") },
                singleLine = true,
            )
        }
        item(span = { GridItemSpan(3) }) {
            OutlinedTextField(
                value = connectionInfo.port.toString(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = {
                    if (it.matches(POSITIVE_NUMBER)) {
                        val p = it.toIntOrNull() ?: 1
                        connectionInfo.port = p.coerceIn(1..65535)
                    }
                },
                label = { Text("Port") },
                singleLine = true,
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = connectionInfo.lobbyName,
                onValueChange = {
                    connectionInfo.lobbyName = it
                },
                label = { Text("Lobby name") },
                singleLine = true,
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedButton(onClick = { gameTypeDropdownExpanded = true }) {
                Text(text = "Selected game type: ${connectionInfo.gameType.string}")
            }
            DropdownMenu(
                expanded = gameTypeDropdownExpanded,
                onDismissRequest = { gameTypeDropdownExpanded = false },
            ) {
                GameType.entries.forEach { type ->
                    DropdownMenuItem(
                        onClick = {
                            gameTypeDropdownExpanded = false
                            connectionInfo.gameType = type
                        }
                    )
                    {
                        Text(text = type.string)
                    }
                }
            }
        }
    }
}

@Composable
private fun LobbyInputView(
    state: ConnectionUIState,
) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(state.trigger) {
        if (state.status.isError) {
            for (i in 0..6) {
                when (i % 2) {
                    0 -> shake.animateTo(2f, spring(stiffness = 60_000f))
                    else -> shake.animateTo(-2f, spring(stiffness = 60_000f))
                }
            }
            shake.animateTo(0f)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(20.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Connection",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = state.connectionInfo.userName,
                onValueChange = { state.connectionInfo.userName = it },
                label = { Text("User name") },
                singleLine = true,
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedButton(onClick = state::connect) {
                Text(text = "Join")
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = state.status.string,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                color =
                    if (state.status.isError)
                        MaterialTheme.colors.error
                    else
                        MaterialTheme.colors.onSurface,
                modifier = Modifier.offset { IntOffset(shake.value.roundToInt(), 0) }
            )
        }
    }
}
