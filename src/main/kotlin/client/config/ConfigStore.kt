package client.config

import game.GameType
import game.gameTypeOf

val CONNECTION_INFO_FILE = "connection_info"

internal object ConfigStore {
    private val connectionInfoConfig = ConfigFile(CONNECTION_INFO_FILE, hashMapOf(
        "ip" to "hilpert.dev",
        "port" to "6666",
        "lobby" to "",
        "game" to GameType.TIC_TAC_TOE.string,
        "user" to "",
    ))

    var ip: String
        set(value) = connectionInfoConfig.set("ip", value)
        get() = connectionInfoConfig.getString("ip")

    var port: Int
        set(value) = connectionInfoConfig.set("port", value)
        get() {
            val port = connectionInfoConfig.getInt("port")
            return if (port != -1) {
                port
            } else {
                1
            }
        }

    var lobby: String
        set(value) = connectionInfoConfig.set("lobby", value)
        get() = connectionInfoConfig.getString("lobby")

    var game: GameType
        set(value) = connectionInfoConfig.set("game", value.string)
        get() = gameTypeOf(connectionInfoConfig.getString("game")) ?: GameType.TIC_TAC_TOE

    var user: String
        set(value) = connectionInfoConfig.set("user", value)
        get() = connectionInfoConfig.getString("user")

    fun storeConnectionInfo() {
        connectionInfoConfig.store()
    }
}