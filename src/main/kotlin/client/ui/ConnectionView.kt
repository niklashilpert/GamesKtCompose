package client.ui

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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import server.GameType
import kotlin.math.roundToInt

@Composable
fun ConnnectionInputView(
    onAddressChange: (String) -> Unit,
    onPortChange: (Int) -> Unit,
    onLobbyNameChange: (String) -> Unit,
    onGameTypeChange: (GameType) -> Unit,
) {
    var ipAddress by remember { mutableStateOf(TextFieldValue()) }
    var port by remember { mutableStateOf(TextFieldValue()) }
    var lobbyName by remember { mutableStateOf(TextFieldValue()) }
    var gameTypeDropdownExpanded by remember { mutableStateOf(false) }
    var gameType by remember { mutableStateOf(GameType.TIC_TAC_TOE) }

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
                value = ipAddress,
                onValueChange = {
                    ipAddress = it
                    onAddressChange(ipAddress.text)
                },
                label = { Text("IP address / Domain name") },
                singleLine = true,
            )
        }

        item(span = { GridItemSpan(3) }) {
            OutlinedTextField(
                value = port,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = {
                    port = it
                    onPortChange(it.text.toIntOrNull() ?: -1)
                },
                label = { Text("Port") },
                singleLine = true,
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = lobbyName,
                onValueChange = {
                    lobbyName = it
                    onLobbyNameChange(lobbyName.text)
                },
                label = { Text("Lobby name") },
                singleLine = true,
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedButton(
                onClick = {gameTypeDropdownExpanded = true},
            ) {
                Text(text = "Selected game type: ${gameType.string}")
            }

            DropdownMenu(
                expanded = gameTypeDropdownExpanded,
                onDismissRequest = { gameTypeDropdownExpanded = false },
            ) {
                GameType.entries.forEach { type ->
                    DropdownMenuItem(
                        onClick = {
                            gameTypeDropdownExpanded = false
                            gameType = type
                            onGameTypeChange(type)
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
fun LobbyInputView(onUserNameChange: (String) -> Unit, onJoin: () -> String?) {
    var userName by remember { mutableStateOf(TextFieldValue()) }

    var errorMsg by remember { mutableStateOf("") }

    val shake = remember { Animatable(0f) }
    var trigger by remember { mutableStateOf(0L) }
    LaunchedEffect(trigger) {
        if (trigger != 0L) {
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
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = userName,
                onValueChange = {
                    userName = it
                    onUserNameChange(userName.text)
                },
                label = { Text("User name") },
                singleLine = true,
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedButton(
                onClick = {
                    val result = onJoin()
                    errorMsg = result ?: ""
                    if (errorMsg != "") {
                        trigger = System.currentTimeMillis()
                    }
                }
            ) {
                Text(text = "Join")
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {


            Text(
                text = errorMsg,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                color = MaterialTheme.colors.error,
                modifier = Modifier.offset { IntOffset(shake.value.roundToInt(), 0)}
            )
        }
    }
}

@Composable
fun ConnectionView(onJoin: (String, Int, String, GameType, String) -> String?) {
    val padding = 10.dp

    var ipAddress by remember { mutableStateOf("") }
    var port by remember { mutableStateOf(0) }
    var lobbyName by remember { mutableStateOf("") }
    var gameType by remember { mutableStateOf(GameType.TIC_TAC_TOE) }
    var userName by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth(.5f)
            .fillMaxHeight()
            .padding(padding)

    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = padding)
        ) {
            ConnnectionInputView(
                {ipAddress = it},
                {port = it},
                {lobbyName = it},
                {gameType = it}
            )
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
                .padding(start = padding)
        ) {
            LobbyInputView(
                { userName = it },
                { onJoin(ipAddress, port, lobbyName, gameType, userName) },
            )
        }

    }
}