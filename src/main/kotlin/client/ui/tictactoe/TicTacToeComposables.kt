package client.ui.tictactoe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import game.tictactoe.TicTacToe
import server.lobby.TicTacToeLobby

private const val CROSS_PATH = "tictactoe/Cross.svg"
private const val RING_PATH = "tictactoe/Ring.svg"
private const val EMPTY_PATH = "tictactoe/Empty.svg"

@Composable
internal fun TicTacToeView(state: TicTacToeUIState) {
    val lobbyInfo by state.lobbyInfoFlow.collectAsState()

    if (lobbyInfo.isOpen) {
        LobbyView(lobbyInfo, state)
    } else {
        GameView(lobbyInfo, state)
    }
}

@Composable
private fun GameView(lobbyInfo: TicTacToeLobby.Info, state: TicTacToeUIState) {
    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
    ) {
        val isPlayerX = (lobbyInfo.playerXName == state.playerName)
        val isPlayersTurn = isPlayerX == lobbyInfo.ticTacToeInfo!!.currentPlayerIsX

        Game_HeaderBar(state)

        Row(modifier = Modifier) {
            Game_GridView(lobbyInfo, state, isPlayersTurn)
            Game_InfoView(lobbyInfo, state, isPlayersTurn, isPlayerX)
        }
    }
}

@Composable
private fun Game_HeaderBar(state: TicTacToeUIState) {
    Row(
        modifier = Modifier,
    ) {
        TextButton(
            onClick = state::disconnect,
        ) {
            Text("Leave")
        }
    }
}

@Composable
private fun Game_GridView(lobbyInfo: TicTacToeLobby.Info, state: TicTacToeUIState, isPlayersTurn: Boolean) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
    ) {
        LazyColumn(
            modifier = Modifier.background(Color.Black),
        ) {
            val gameInfo = lobbyInfo.ticTacToeInfo!!

            items(3) { y ->
                LazyRow(modifier = Modifier) {
                    items(3) { x ->
                        val (imageResource, description) = when (gameInfo.getOccupation(x, y)) {
                            TicTacToe.CellOccupation.FREE -> EMPTY_PATH to " "
                            TicTacToe.CellOccupation.X_OCCUPIED -> CROSS_PATH to "X"
                            TicTacToe.CellOccupation.O_OCCUPIED -> RING_PATH to "O"
                        }
                        Box(modifier = Modifier.padding(2.dp)) {
                            TextButton(
                                enabled = isPlayersTurn && gameInfo.inProgress,
                                modifier = Modifier.padding(0.dp).background(Color.White),
                                onClick = {
                                    state.placeMark(x, y)
                                }
                            ) {
                                Image(
                                    painter = painterResource(imageResource),
                                    contentDescription = description,
                                    modifier = Modifier.size(150.dp, 150.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Game_InfoView(lobbyInfo: TicTacToeLobby.Info, state: TicTacToeUIState, isPlayersTurn: Boolean, isPlayerX: Boolean) {
    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        val gameInfo = lobbyInfo.ticTacToeInfo!!
        val otherPlayer = if (isPlayerX) lobbyInfo.playerOName else lobbyInfo.playerXName
        val playerSymbol = if (isPlayerX) "X" else "O"

        if (gameInfo.inProgress) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("Your symbol: $playerSymbol")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(if (isPlayersTurn) "Your turn" else "${otherPlayer}'s turn")
            }
        } else {
            var endText = if (gameInfo.xWon && isPlayerX || gameInfo.oWon && !isPlayerX) {
                "You won! :)"
            } else if (!gameInfo.tie) {
                "$otherPlayer won! :("
            } else {
                "It's a tie! :/"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(endText)
            }
            if (lobbyInfo.hostName == state.playerName) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = { state.stopGame() }
                    ) {
                        Text("Return to lobby")
                    }
                }
            }
        }
    }
}


@Composable
fun LobbyView(lobbyInfo: TicTacToeLobby.Info, state: TicTacToeUIState) {
    Column(
        modifier = Modifier.padding(16.dp),
    ) {
        Lobby_HeaderBar(state)
        Lobby_PlayerDisplay(lobbyInfo)
        Lobby_FooterBar(lobbyInfo, state)
    }
}

@Composable
fun Lobby_HeaderBar(state: TicTacToeUIState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        TextButton(
            onClick = state::disconnect,
        ) {
            Text("Leave")
        }
    }
}

@Composable
fun Lobby_PlayerDisplay(lobbyInfo: TicTacToeLobby.Info) {
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
fun Lobby_FooterBar(lobbyInfo: TicTacToeLobby.Info, state: TicTacToeUIState) {
    val isHost = lobbyInfo.hostName == state.playerName
    val canStartGame = lobbyInfo.playerXName != null && lobbyInfo.playerOName != null
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        if (isHost) {
            TextButton(onClick = state::swapPlayers) {
                Text("Swap positions")
            }
            Spacer(Modifier.width(16.dp))
            Button(
                onClick = state::startGame,
                enabled = canStartGame,

                ) {
                Text("Start")
            }
        }
    }
}