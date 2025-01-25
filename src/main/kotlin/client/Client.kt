package client

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import client.ui.connection.ConnectionUIState
import client.ui.tictactoe.TicTacToeUIState
import client.ui.UIState
import client.ui.connection.ConnectionView
import client.ui.tictactoe.TicTacToeView


@Composable
@Preview
fun App(uiState: UIState, scaffoldState: ScaffoldState) {
    MaterialTheme {
        Scaffold (
            scaffoldState = scaffoldState,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colors.background
            ) {
                when (uiState) {
                    is ConnectionUIState -> {
                        ConnectionView(uiState)
                    }
                    is TicTacToeUIState -> {
                        TicTacToeView(uiState)
                    }
                    else -> {}
                }
            }
        }
    }
}

fun main() {
    TicTacToeUIState::javaClass.javaClass.getResourceAsStream("/tictactoe/Cross.svg")

    application {
        val scaffoldState = rememberScaffoldState()
        val coroutineScope = rememberCoroutineScope()
        var uiState: UIState by remember { mutableStateOf(ConnectionUIState(scaffoldState, coroutineScope)) }
        remember { uiState.onStateChange = { it ->
            uiState = it
        }}

        val windowState = rememberWindowState()
        windowState.size = DpSize(900.dp, 700.dp)

        Window(
            onCloseRequest = ::exitApplication,
            title = uiState.title,
            resizable = false,
            state = windowState,
        ) {
           App(uiState, scaffoldState)
        }
    }
}
