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
import client.config.ConfigStore
import server.GameType
import kotlin.math.roundToInt

val POSITIVE_NUMBER = Regex("^\\d+$")

object ConnectionUIHandle {

    enum class State(val string: String, val isError: Boolean) {
        INVALID_IP("Please enter a valid IP address", true),
        INVALID_LOBBY("Please enter a Lobby name", true),
        INVALID_USER_NAME("Please enter a user name", true),
        NO_CONNECTION("Could not connect to the provided server details", true),
        IDLE("", false),
        CONNECTING("Connecting...", false)
    }

    private var _ipAddress by mutableStateOf(ConfigStore.ip)
    var ipAddress: String
        get() = _ipAddress
        set(value) {
            _ipAddress = value
            ConfigStore.ip = value
        }

    private var _port by mutableStateOf(ConfigStore.port)
    var port: Int
        get() = _port
        set(value) {
            _port = value
            ConfigStore.port = value
        }

    private var _lobbyName by mutableStateOf(ConfigStore.lobby)
    var lobbyName: String
        get() = _lobbyName
        set(value) {
            _lobbyName = value
            ConfigStore.lobby = value
        }

    private var _gameType by mutableStateOf(ConfigStore.game)
    var gameType: GameType
        get() = _gameType
        set(value) {
            _gameType = value
            ConfigStore.game = value
        }

    private var _userName by mutableStateOf(ConfigStore.user)
    var userName: String
        get() = _userName
        set(value) {
            _userName = value
            ConfigStore.user = value
        }

    var state by mutableStateOf(State.IDLE)
        private set

    private var stateTrigger by mutableStateOf(0L)

    fun updateState(state: State) {
        this.state = state
        stateTrigger++
    }

    private fun onClick(onJoin: (ConnectionUIHandle) -> Unit) {
        updateStateWithConnectionInfo()
        if (!state.isError) {
            ConfigStore.storeConnectionInfo()
            onJoin(this)
        }
    }

    private fun updateStateWithConnectionInfo() {
        if (ipAddress.isBlank()) {
            updateState(State.INVALID_IP)
        } else if (lobbyName.isBlank()) {
            updateState(State.INVALID_LOBBY)
        } else if (userName.isBlank()) {
            updateState(State.INVALID_USER_NAME)
        } else {
            updateState(State.IDLE)
        }
    }

    @Composable
    fun ConnectionView(onJoin: (ConnectionUIHandle) -> Unit) {
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
                ConnectionInputView()
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
                LobbyInputView() { onClick(onJoin) }
            }

        }
    }

    @Composable
    private fun ConnectionInputView() {
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
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("IP address / Domain name") },
                    singleLine = true,
                )
            }

            item(span = { GridItemSpan(3) }) {
                OutlinedTextField(
                    value = port.toString(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        if (it.matches(POSITIVE_NUMBER)) {
                            val p = it.toIntOrNull() ?: 1
                            port = p.coerceIn(1..65535)
                        }
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
                    },
                    label = { Text("Lobby name") },
                    singleLine = true,
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedButton(
                    onClick = { gameTypeDropdownExpanded = true },
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
    private fun LobbyInputView(onJoin: () -> Unit) {

        val shake = remember { Animatable(0f) }
        LaunchedEffect(stateTrigger) {
            if (state.isError) {
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
                    text = state.string,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    color = if (state.isError) MaterialTheme.colors.error else MaterialTheme.colors.onSurface,
                    modifier = Modifier.offset { IntOffset(shake.value.roundToInt(), 0) }
                )
            }
        }
    }
}