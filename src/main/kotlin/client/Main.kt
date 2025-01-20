package client

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import client.ui.ConnectionUIManager
import client.ui.TicTacToeUIManager
import game.GameType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import shared.connection.PacketConnection
import shared.connection.InetPacket
import shared.connection.Player
import shared.connection.ResponseCode
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

enum class WindowType(val title: String) {
    CONNECTION_INPUT("GamesKt"),
    TIC_TAC_TOE_LOBBY("GamesKt - TicTacToe"),
    CHESS_LOBBY("GamesKt - Chess"),
    CHECKERS_LOBBY("GamesKt - Checkers"),
}

class SnackbarView(private val scaffoldState: ScaffoldState, private val coroutineScope: CoroutineScope) {
    fun showMessage(message: String) {
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Dismiss",
            )
        }
    }
}

private var player: Player? = null

private var windowType by mutableStateOf(WindowType.CONNECTION_INPUT)

@Composable
@Preview
fun App() {
    MaterialTheme {
        val scaffoldState = rememberScaffoldState()
        val coroutineScope = rememberCoroutineScope()

        val snackbar = remember { SnackbarView(scaffoldState, coroutineScope) }

        Scaffold (
            scaffoldState = scaffoldState
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colors.background
            ) {
                when (windowType) {
                    WindowType.CONNECTION_INPUT -> {
                        ConnectionUIManager(::connect).ConnectionView()
                    }
                    WindowType.TIC_TAC_TOE_LOBBY -> {
                        if (player != null) {
                            TicTacToeUIManager(
                                player!!,
                                snackbar::showMessage,
                                {
                                    player!!.close()
                                    player = null
                                    windowType = WindowType.CONNECTION_INPUT
                                }
                            ).TicTacToeView()
                        } else {
                            windowType = WindowType.CONNECTION_INPUT
                            snackbar.showMessage("An unknown error occurred: Player object is null.")
                        }
                    }
                    WindowType.CHESS_LOBBY -> {
                        windowType = WindowType.CONNECTION_INPUT
                        snackbar.showMessage("Not implemented yet")
                    }
                    WindowType.CHECKERS_LOBBY -> {
                        windowType = WindowType.CONNECTION_INPUT
                        snackbar.showMessage("Not implemented yet")
                    }
                }
            }
        }
    }
}

fun connect(uiHandle: ConnectionUIManager) {
    uiHandle.updateState(ConnectionUIManager.State.CONNECTING)
    Thread {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(uiHandle.ipAddress, uiHandle.port))
            val newPlayer = Player(PacketConnection(socket, true), uiHandle.userName)
            newPlayer.write(InetPacket.Connect(uiHandle.userName, uiHandle.lobbyName, uiHandle.gameType))
            val response = newPlayer.read()
            if (response is InetPacket.Response) {
                when (response.code) {
                    ResponseCode.SUCCESS -> {
                        player = newPlayer
                        windowType = when (uiHandle.gameType) {
                            GameType.TIC_TAC_TOE -> WindowType.TIC_TAC_TOE_LOBBY
                            GameType.CHESS -> WindowType.CHESS_LOBBY
                            GameType.CHECKERS -> WindowType.CHECKERS_LOBBY
                        }
                    }
                    ResponseCode.LOBBY_IS_FULL -> {
                        uiHandle.updateState(ConnectionUIManager.State.LOBBY_IS_FULL)
                    }
                    ResponseCode.LOBBY_IS_PLAYING -> {
                        uiHandle.updateState(ConnectionUIManager.State.LOBBY_IS_PLAYING)
                    }
                    ResponseCode.PLAYER_EXISTS -> {
                        uiHandle.updateState(ConnectionUIManager.State.PLAYER_EXISTS)
                    }
                    else -> {
                        uiHandle.updateState(ConnectionUIManager.State.UNKNOWN_ERROR)
                    }
                }
            } else {
                uiHandle.updateState(ConnectionUIManager.State.UNKNOWN_ERROR)
            }
        } catch (e: IOException) {
            uiHandle.updateState(ConnectionUIManager.State.NO_CONNECTION)
        }
    }.start()
}

fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = windowType.title,
            resizable = false,
        ) {
           App()
        }
    }
}
