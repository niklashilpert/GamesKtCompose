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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import server.GameType
import kotlin.math.roundToInt

val POSITIVE_NUMBER = Regex("^\\d+$")

class ConnectionUIHandle {

    enum class State(val string: String, val isError: Boolean) {
        INVALID_IP("Please enter a valid IP address", true),
        INVALID_LOBBY("Please enter a Lobby name", true),
        INVALID_USER_NAME("Please enter a user name", true),
        NO_CONNECTION("Could not connect to the provided server details", true),
        IDLE("", false),
        CONNECTING("Connecting...", false)
    }

    class Info internal constructor(ipAddress: String, port: Int, lobbyName: String, gameType: GameType, userName: String) {
        var ipAddress by mutableStateOf(ipAddress)
            internal set
        var port by mutableStateOf(port)
            internal set
        var lobbyName by mutableStateOf(lobbyName)
            internal set
        var gameType by mutableStateOf(gameType)
            internal set
        var userName by mutableStateOf(userName)
            internal set
    }

    var state by mutableStateOf(State.IDLE)
        private set

    private var stateTrigger by mutableStateOf(0L)

    private val info = getConnectionInfoFromDefaults()

    fun updateState(state: State) {
        this.state = state
        stateTrigger++
    }

    private fun updateStateWithConnectionInfo() {
        if (info.ipAddress.isBlank()) {
            updateState(State.INVALID_IP)
        } else if (info.lobbyName.isBlank()) {
            updateState(State.INVALID_LOBBY)
        } else if (info.userName.isBlank()) {
            updateState(State.INVALID_USER_NAME)
        } else {
            updateState(State.IDLE)
        }
    }

    private fun getConnectionInfoFromDefaults(): Info {
        val ipAddress = "localhost"
        val port = 5555
        val lobbyName = "lobby"
        val gameType = GameType.TIC_TAC_TOE
        val userName = ""
        return Info(ipAddress, port, lobbyName, gameType, userName)
    }

    @Composable
    fun ConnectionView(uiHandle: ConnectionUIHandle, onJoin: (ConnectionUIHandle) -> Unit) {
        val padding = 10.dp

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
                ConnectionInputView(uiHandle)
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
                    uiHandle,
                ) {
                    uiHandle.updateStateWithConnectionInfo()
                    if (!uiHandle.state.isError) {
                        onJoin(uiHandle)
                    }
                }
            }

        }
    }

    @Composable
    private fun ConnectionInputView(uiHandle: ConnectionUIHandle) {
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
                    value = uiHandle.info.ipAddress,
                    onValueChange = {
                        uiHandle.info.ipAddress = it
                    },
                    label = { Text("IP address / Domain name") },
                    singleLine = true,
                )
            }

            item(span = { GridItemSpan(3) }) {
                OutlinedTextField(
                    value = uiHandle.info.port.toString(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        if (it.matches(POSITIVE_NUMBER)) {
                            val port = it.toIntOrNull() ?: 1
                            uiHandle.info.port = port.coerceIn(1..65535)
                        }
                    },
                    label = { Text("Port") },
                    singleLine = true,
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = uiHandle.info.lobbyName,
                    onValueChange = {
                        uiHandle.info.lobbyName = it
                    },
                    label = { Text("Lobby name") },
                    singleLine = true,
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedButton(
                    onClick = { gameTypeDropdownExpanded = true },
                ) {
                    Text(text = "Selected game type: ${uiHandle.info.gameType.string}")
                }

                DropdownMenu(
                    expanded = gameTypeDropdownExpanded,
                    onDismissRequest = { gameTypeDropdownExpanded = false },
                ) {
                    GameType.entries.forEach { type ->
                        DropdownMenuItem(
                            onClick = {
                                gameTypeDropdownExpanded = false
                                uiHandle.info.gameType = type
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
    private fun LobbyInputView(uiHandle: ConnectionUIHandle, onJoin: () -> Unit) {

        val shake = remember { Animatable(0f) }
        LaunchedEffect(uiHandle.stateTrigger) {
            if (uiHandle.state.isError) {
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
                    value = uiHandle.info.userName,
                    onValueChange = {
                        uiHandle.info.userName = it
                    },
                    label = { Text("User name") },
                    singleLine = true,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedButton(
                    onClick = { onJoin() }
                ) {
                    Text(text = "Join")
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = uiHandle.state.string,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    color = if (uiHandle.state.isError) MaterialTheme.colors.error else MaterialTheme.colors.onSurface,
                    modifier = Modifier.offset { IntOffset(shake.value.roundToInt(), 0) }
                )
            }
        }
    }
}