package client.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import server.lobby.TicTacToeLobby
import shared.connection.InetPacket
import shared.connection.Player
import shared.connection.ResponseCode
import shared.connection.TicTacToePackets
import java.io.IOException

class TicTacToeUIManager(
    private val player: Player,
    private val displayError: (message: String) -> Unit,
    private val onDisconnect: () -> Unit,
) {

    private var keepListening = true

    init {
        startListenerCoroutine()
    }

    private var lobbyInfoFlow = MutableStateFlow(TicTacToeLobby.Info(
        lobbyName = "[lobby_name]",
        isOpen = true,
        playerXName = "[player_x]",
        playerOName = "[player_o]",
        hostName = "[host]",
        ticTacToeInfo = null,
    ))

    private fun disconnect() {
        keepListening = false
        player.close()
        onDisconnect()
    }

    private fun startListenerCoroutine() {
        Thread {
            while (keepListening && !player.isClosed) {
                try {
                    val packet = player.read()
                    handlePacket(packet)
                } catch (e: IOException) {
                    player.close()
                }
            }
            println("Stopping player thread for $player.")
        }.start()

    }

    private fun handlePacket(packet: InetPacket) {
        when (packet) {
            is InetPacket.Response -> handleResponse(packet.code)
            is TicTacToePackets.LobbyInfo -> {
                lobbyInfoFlow.value = packet.lobbyInfo
            }
            else -> {
                displayError("An unexpected error occurred. (Packet: ${packet.javaClass.canonicalName})")
                disconnect()
                player.close()
            }
        }
    }

    private fun handleResponse(code: ResponseCode) {
        /*when (code) {
            else -> {}
        }*/
    }

    private fun sendPacketOrDisplayErrorAndClose(packet: InetPacket) {
        try {
            player.write(packet)
        } catch (e: IOException) {
            disconnect()
            displayError("The connection was closed unexpectedly. (Packet: ${packet.javaClass.canonicalName})")
        }
    }

    private fun swapPlayers() {
        sendPacketOrDisplayErrorAndClose(InetPacket.SwapPlayers())
    }

    private fun startGame() {
        sendPacketOrDisplayErrorAndClose(InetPacket.StartGame())
    }


    @Composable
    fun TicTacToeView() {
        val lobbyInfo by lobbyInfoFlow.collectAsState()

        if (lobbyInfo.isOpen) {
            LobbyView(lobbyInfo)
        } else {
            Text("Playing")
        }


    }

    @Composable
    fun LobbyView(lobbyInfo: TicTacToeLobby.Info) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            HeaderBar()
            PlayerPanel(lobbyInfo)
            FooterBar(lobbyInfo)
        }
    }

    @Composable
    fun HeaderBar() {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            TextButton(
                onClick = ::disconnect
            ) {
                Text("Leave")
            }
        }
    }

    @Composable
    fun PlayerPanel(lobbyInfo: TicTacToeLobby.Info) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Player X",
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(lobbyInfo.playerXName ?: "")
            }

            Spacer(Modifier.width(16.dp))
            Text("vs.")
            Spacer(Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Player O",
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(lobbyInfo.playerOName ?: "")
            }
        }
    }

    @Composable
    fun FooterBar(lobbyInfo: TicTacToeLobby.Info) {
        val isHost = lobbyInfo.hostName == player.name
        val canStartGame = lobbyInfo.playerXName != null && lobbyInfo.playerOName != null
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            if (isHost) {
                TextButton(onClick = ::swapPlayers) {
                    Text("Swap positions")
                }
                Spacer(Modifier.width(16.dp))
                Button(
                    onClick = ::startGame,
                    enabled = canStartGame,

                    ) {
                    Text("Start")
                }
            }
        }
    }
}