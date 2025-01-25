package client.ui

import client.ui.connection.ConnectionUIState
import shared.connection.InetPacket
import shared.connection.Player
import java.io.IOException

abstract class LobbyUIState(protected val player: Player, uiState: UIState) : UIState(uiState) {

    val playerName get() = player.name

    private var keepListening = true

    init {
        runListenerThread()
    }

    abstract fun handlePacket(packet: InetPacket)

    private fun runListenerThread() {
        Thread {
            while (keepListening && !player.isClosed) {
                try {
                    val packet = player.read()
                    handlePacket(packet)
                } catch (e: IOException) {
                    disconnect()
                }
            }
            println("Stopping player thread for $player.")
        }.start()
    }

    protected fun sendPacket(packet: InetPacket) {
        try {
            player.write(packet)
        } catch (e: IOException) {
            triggerSnackbar("The connection was closed unexpectedly.")
            disconnect()
        }
    }

    fun disconnect() {
        if (keepListening) {
            keepListening = false
            player.close()
            onStateChange(ConnectionUIState(this))
        }
    }
}