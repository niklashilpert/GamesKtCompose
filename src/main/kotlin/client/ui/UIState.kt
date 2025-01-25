package client.ui

import androidx.compose.material.ScaffoldState
import client.connection.ConnectionInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class UIState protected constructor(
    private val scaffoldState: ScaffoldState,
    private val coroutineScope: CoroutineScope,
    val connectionInfo: ConnectionInfo,
    var onStateChange: (UIState) -> Unit = {},
) {
    constructor(uiState: UIState) : this(
        uiState.scaffoldState,
        uiState.coroutineScope,
        uiState.connectionInfo,
        uiState.onStateChange
    )

    abstract val title: String

    fun triggerSnackbar(message: String) {
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Dismiss",
            )
        }
    }
}