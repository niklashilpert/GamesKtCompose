package client

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import client.ui.ConnectionUIHandle
import java.lang.Thread.sleep


@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.background
        ) {
            ConnectionUIHandle.ConnectionView(::connect)
        }
    }
}


fun connect(uiHandle: ConnectionUIHandle) {
    uiHandle.updateState(ConnectionUIHandle.State.CONNECTING)
    Thread {
        sleep(1000)
        uiHandle.updateState(ConnectionUIHandle.State.NO_CONNECTION)
    }.start()
}

fun main() {
    application {
        val windowTitle = remember { mutableStateOf("GamesKt") }
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
