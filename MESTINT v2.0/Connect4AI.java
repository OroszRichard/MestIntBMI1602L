import java.util.List;

/**
 * Connect4 Mesterséges Intelligencia modul integráló osztálya.
 * Összefogja a tábla állapotteret és a heurisztikus kiértékelőt.
 */
public class Connect4AI {

    private final char aiPlayer = 'R';
    private final char humanPlayer = 'Y';
    private final int maxDepth;
    private final BoardEvaluator evaluator;

    /**
     * Konstruktor a mesterséges intelligencia inicializálásához.
     * 
     * @param maxDepth A minimax algoritmus keresési mélysége (előretekintés száma).
     */
    public Connect4AI(int maxDepth) {
        this.maxDepth = maxDepth;
        this.evaluator = new BoardEvaluator(); // Függőség inicializálása
    }

    /**
     * Visszaadja a legjobb oszlopot a megadott táblaállapot alapján.
     * 
     * @param initialBoardState A játék aktuális állapota nyers tömb formájában.
     * @return Az oszlop indexe, ahova az AI lépni szeretne (0. index az első
     *         oszlop).
     */
    public int getBestColumn(char[][] initialBoardState) {
        // Állapottér inicializálása saját objektumba
        Connect4Board board = new Connect4Board(initialBoardState);
        List<Integer> validLocations = board.getValidLocations();

        // Ha nincs hová lépni
        if (validLocations.isEmpty()) {
            return -1;
        }

        int bestScore = Integer.MIN_VALUE;
        int bestCol = -1;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // Az összes érvényes oszlop végigpróbálása
        for (int col : validLocations) {
            int row = board.getNextOpenRow(col);
            board.dropPiece(row, col, aiPlayer); // Képzeletbeli lépés

            // Minimax elindítása csökkentett mélységgel, és az ellenfél jön (isMaximizing =
            // false)
            int score = minimax(board, maxDepth - 1, alpha, beta, false);

            board.undoMove(row, col); // Lépés visszavonása

            if (score > bestScore) {
                bestScore = score;
                bestCol = col;
            }
        }

        // Biztonsági (fallback) ellenőrzés
        if (bestCol == -1 && !validLocations.isEmpty()) {
            bestCol = validLocations.get(0);
        }

        return bestCol;
    }

    /**
     * A Minimax algoritmus rekurzív megvalósítása alpha-beta nyeséssel.
     */
    private int minimax(Connect4Board board, int depth, int alpha, int beta, boolean isMaximizing) {
        boolean isTerminal = board.isTerminalNode(aiPlayer, humanPlayer);

        if (depth == 0 || isTerminal) {
            if (isTerminal) {
                if (board.checkWin(aiPlayer))
                    return 10000000;
                else if (board.checkWin(humanPlayer))
                    return -10000000;
                else
                    return 0; // Betelt a pálya / Döntetlen
            } else {
                return evaluator.evaluate(board, aiPlayer, humanPlayer);
            }
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int col : board.getValidLocations()) {
                int row = board.getNextOpenRow(col);
                board.dropPiece(row, col, aiPlayer);
                int eval = minimax(board, depth - 1, alpha, beta, false);
                board.undoMove(row, col);

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Beta nyesés
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int col : board.getValidLocations()) {
                int row = board.getNextOpenRow(col);
                board.dropPiece(row, col, humanPlayer);
                int eval = minimax(board, depth - 1, alpha, beta, true);
                board.undoMove(row, col);

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Alpha nyesés
                }
            }
            return minEval;
        }
    }
}
