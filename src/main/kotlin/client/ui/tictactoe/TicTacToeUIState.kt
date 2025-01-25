package client.ui.tictactoe

import client.ui.LobbyUIState
import client.ui.UIState
import kotlinx.coroutines.flow.MutableStateFlow
import server.lobby.TicTacToeLobby
import shared.connection.InetPacket
import shared.connection.Player
import shared.connection.ResponseCode
import shared.connection.TicTacToePackets

class TicTacToeUIState(player: Player, uiState: UIState) : LobbyUIState(player, uiState) {
    override val title: String
        get() = "GamesKt - TicTacToe - [X] vs. [O] ([lobby name])"

    var lobbyInfoFlow = MutableStateFlow(
        TicTacToeLobby.Info(
        lobbyName = "[lobby_name]",
        isOpen = true,
        playerXName = "[player_x]",
        playerOName = "[player_o]",
        hostName = "[host]",
        ticTacToeInfo = null,
    ))
        private set

    override fun handlePacket(packet: InetPacket) {
        when (packet) {
            is InetPacket.Response -> handleResponse(packet.code)
            is TicTacToePackets.LobbyInfo -> {
                lobbyInfoFlow.value = packet.lobbyInfo
            }
            else -> {
                triggerSnackbar("An unexpected error occurred. (Packet: ${packet.javaClass.canonicalName})")
                disconnect()
            }
        }
    }

    private fun handleResponse(code: ResponseCode) {
        /*when (code) {
            else -> {}
        }*/
    }

    fun swapPlayers() {
        sendPacket(InetPacket.SwapPlayers())
    }

    fun startGame() {
        sendPacket(InetPacket.StartGame())
    }

    fun stopGame() {
        sendPacket(InetPacket.StopGame())
    }

    fun placeMark(x: Int, y: Int) {
        sendPacket(TicTacToePackets.PlaceMark(x, y))
    }
}