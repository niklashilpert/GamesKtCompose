package client.ui.connection

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import client.config.ConfigStore
import client.connection.ConnectionInfo
import client.ui.tictactoe.TicTacToeUIState
import client.ui.UIState
import game.GameType
import kotlinx.coroutines.CoroutineScope
import shared.connection.InetPacket
import shared.connection.PacketConnection
import shared.connection.Player
import shared.connection.ResponseCode
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

const val CONNECTION_TIMEOUT = 3000

class ConnectionUIState : UIState {
    constructor(uiState: UIState) : super(uiState)
    internal constructor(
        scaffoldState: ScaffoldState,
        coroutineScope: CoroutineScope,
    ) : super(scaffoldState, coroutineScope, ConnectionInfo())


    enum class StatusCode(val string: String, val isError: Boolean) {
        INVALID_IP("Please enter a valid IP address", true),
        INVALID_LOBBY("Please enter a lobby name", true),
        INVALID_USER_NAME("Please enter a user name", true),
        NO_CONNECTION("Could not connect to the provided server details", true),
        EMPTY("", false),
        CONNECTING("Connecting...", false),

        LOBBY_IS_FULL("The lobby is full", true),
        LOBBY_IS_PLAYING("The lobby is currently playing", true),
        PLAYER_EXISTS("A player with that name already joined the lobby", true),
    }

    override val title: String
        get() = "GamesKt - Connect to a server"

    var trigger by mutableStateOf(System.currentTimeMillis())
        private set

    private var _state = mutableStateOf(StatusCode.EMPTY)
    var status
        get() = _state.value
        set(value) {
            _state.value = value
            trigger = System.currentTimeMillis()
        }

    /**
     * Tries to create a player instance with the current connection info of the UI state.
     * If any errors occur, they will be displayed in the display text field
     * (or in a snackbar popup if actual errors occur).
     * This function will trigger a change to the associated lobby state if the connection process is successful.
     */
    fun connect() {
        val isInfoValid = updateStateWithInfo(connectionInfo)
        if (!isInfoValid) {
            return
        }

        status = StatusCode.CONNECTING
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(connectionInfo.ipAddress, connectionInfo.port), CONNECTION_TIMEOUT)
            val player = Player(PacketConnection(socket, true), connectionInfo.userName)

            val isConnected = connectPlayerWithLobby(player, connectionInfo)
            if (isConnected) {
                ConfigStore.ip = connectionInfo.ipAddress
                ConfigStore.port = connectionInfo.port
                ConfigStore.lobby = connectionInfo.lobbyName
                ConfigStore.game = connectionInfo.gameType
                ConfigStore.user = connectionInfo.userName
                ConfigStore.storeConnectionInfo()

                when (connectionInfo.gameType) {
                    GameType.TIC_TAC_TOE -> {
                        onStateChange(TicTacToeUIState(player, this))
                    }

                    else -> {
                        TODO("Other game types are not implemented yet.")
                    }
                }
            }
        } catch (e: IOException) {
            status = StatusCode.NO_CONNECTION
        }
    }

    /**
     * Validates the provided connection info and sets the UI state accordingly.
     * @return True if the information is valid, false if it isn't.
     */
    private fun updateStateWithInfo(info: ConnectionInfo): Boolean {
        if (info.ipAddress.isBlank()) {
            status = StatusCode.INVALID_IP
        } else if (info.lobbyName.isBlank()) {
            status = StatusCode.INVALID_LOBBY
        } else if (info.userName.isBlank()) {
            status = StatusCode.INVALID_USER_NAME
        } else {
            status = StatusCode.EMPTY
            return true
        }
        return false
    }

    /**
     * Tries to connect the provided player with a server lobby.
     * This method updates the connection status to display any expected problems that can occur because of user input.
     * @return True, if the player was successfully connected with the lobby.
     */
    private fun connectPlayerWithLobby(player: Player, connectionInfo: ConnectionInfo): Boolean {
        player.write(InetPacket.Connect(connectionInfo.userName, connectionInfo.lobbyName, connectionInfo.gameType))
        val response = player.read()

        var isSuccess = false
        if (response is InetPacket.Response) {
            when (response.code) {
                ResponseCode.SUCCESS -> {
                    isSuccess = true
                    status = StatusCode.EMPTY
                }

                ResponseCode.LOBBY_IS_FULL -> {
                    status = StatusCode.LOBBY_IS_FULL
                }

                ResponseCode.LOBBY_IS_PLAYING -> {
                    status = StatusCode.LOBBY_IS_PLAYING
                }

                ResponseCode.PLAYER_EXISTS -> {
                    status = StatusCode.PLAYER_EXISTS
                }

                else -> {
                    status = StatusCode.NO_CONNECTION
                }
            }
        } else {
            status = StatusCode.NO_CONNECTION
        }
        return isSuccess
    }
}