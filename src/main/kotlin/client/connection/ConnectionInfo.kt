package client.connection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import client.config.ConfigStore

class ConnectionInfo {
    var ipAddress by mutableStateOf(ConfigStore.ip)
        internal set

    var port by mutableStateOf(ConfigStore.port)
        internal set

    var lobbyName by mutableStateOf(ConfigStore.lobby)
        internal set

    var gameType by mutableStateOf(ConfigStore.game)
        internal set

    var userName by mutableStateOf(ConfigStore.user)
        internal set
}