package client

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import client.ui.ConnectionView
import server.GameType


@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.background
        ) {
            ConnectionView(::connect)
        }
    }
}


fun connect(ipAddress: String, port: Int, lobbyName: String, gameType: GameType, userName: String): String? {
    return "Error"
}

fun main() {

    println("Test")

    application {
        var windowTitle = remember { mutableStateOf("GamesKt") }
        Window(
            onCloseRequest = ::exitApplication,
            title = windowTitle.value,
            //resizable = false,
            // state = WindowState(size = DpSize(700.dp, 500.dp))
        ) {
            App()
        }
    }
}
