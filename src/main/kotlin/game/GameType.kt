package game

enum class GameType(val string: String) {
    TIC_TAC_TOE ("TicTacToe"),
    CHESS ("Chess"),
    CHECKERS ("Checkers"),
}

fun gameTypeOf(string: String): GameType? {
    when (string) {
        "TicTacToe" -> return GameType.TIC_TAC_TOE
        "Chess" -> return GameType.CHESS
        "Checkers" -> return GameType.CHECKERS
        else -> return null
    }
}