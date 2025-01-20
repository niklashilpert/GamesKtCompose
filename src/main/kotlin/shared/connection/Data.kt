package shared.connection

import game.GameType
import server.lobby.TicTacToeLobby
import java.io.Serializable


abstract class DataPacket {
}

enum class ResponseCode : Serializable {
    // General
    SUCCESS,
    NOT_AUTHORIZED,
    LOBBY_IS_FULL,
    LOBBY_IS_NOT_FULL,
    LOBBY_IS_PLAYING,
    LOBBY_IS_OPEN,
    PLAYER_EXISTS,

    // TicTacToe specific
    NOT_YOUR_TURN,
    OUT_OF_BOUNDS,
    PLACE_IS_OCCUPIED,
    GAME_IS_OVER
}

abstract class InetPacket : DataPacket(), Serializable {
    abstract class LobbyInfo : InetPacket()

    class Response(val code: ResponseCode) : InetPacket()

    // Sent from client
    class Connect(val playerName: String, val lobbyName: String, val gameType: GameType) : InetPacket()
    class StatusRequest : InetPacket()

    class StartGame : InetPacket()
    class StopGame : InetPacket()
    class SwapPlayers : InetPacket()
}


abstract class TicTacToePackets {
    // Sent from server
    class LobbyInfo(val lobbyInfo: TicTacToeLobby.Info) : InetPacket.LobbyInfo()

    // Send from client
    class PlaceMark(val x: Int, val y: Int) : InetPacket()
}

