import java.util.List;

/**
 * Connect4 Mesterséges Intelligencia modul integráló osztálya.
 * Összefogja a tábla állapotteret és a heurisztikus kiértékelőt.
 * Részletes naplózást (logging) végez a tesztelés és az algoritmika bemutatása érdekében.
 */
public class Connect4AI {

    private final char aiPlayer = 'R';
    private final char humanPlayer = 'Y';
    private final int maxDepth;
    private final BoardEvaluator evaluator;

    public Connect4AI(int maxDepth) {
        this.maxDepth = maxDepth;
        this.evaluator = new BoardEvaluator();
    }

    public int getBestColumn(char[][] initialBoardState) {
        Connect4Board board = new Connect4Board(initialBoardState);
        List<Integer> validLocations = board.getValidLocations();

        if (validLocations.isEmpty()) {
            return -1;
        }

        int bestScore = Integer.MIN_VALUE;
        int bestCol = -1;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        System.out.println("\n=======================================================");
        System.out.println("Keresés indítása. Aktuális állapotteret vizsgálunk...");
        System.out.println("Maximális mélység (Depth): " + maxDepth);
        System.out.println("Lehetséges lépések száma a gyökéren: " + validLocations.size());

        for (int col : validLocations) {
            int row = board.getNextOpenRow(col);
            board.dropPiece(row, col, aiPlayer);
            
            System.out.println(">> Gyökér szint - Megvizsgáljuk: " + col + ". oszlopot lépésként.");
            
            // Minimax elindítása
            int score = minimax(board, maxDepth - 1, alpha, beta, false);

            board.undoMove(row, col); 
            
            System.out.println("<< " + col + ". oszlop vizsgált értéke (Score): " + score);

            if (score > bestScore) {
                bestScore = score;
                bestCol = col;
                System.out.println("   [!] Új legjobb oszlop kiválasztva: " + bestCol + " (Pontszám: " + bestScore + ")");
            }
        }

        if (bestCol == -1 && !validLocations.isEmpty()) {
            bestCol = validLocations.get(0);
        }

        System.out.println("A keresés befejeződött! A legoptimálisabb döntés: " + bestCol + ". oszlop");
        System.out.println("=======================================================\n");
        return bestCol;
    }

    private int minimax(Connect4Board board, int depth, int alpha, int beta, boolean isMaximizing) {
        boolean isTerminal = board.isTerminalNode(aiPlayer, humanPlayer);

        if (depth == 0 || isTerminal) {
            if (isTerminal) {
                if (board.checkWin(aiPlayer)) return 1000000;
                else if (board.checkWin(humanPlayer)) return -1000000;
                else return 0; // Betelt
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
                    System.out.println("    [Alpha-Beta Nyesés] AI (Maximizing) ágon: alpha(" + alpha + ") >= beta(" + beta + "). Mögöttes játékfa elhagyása.");
                    break;
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
                    System.out.println("    [Alpha-Beta Nyesés] Játékos (Minimizing) ágon: beta(" + beta + ") <= alpha(" + alpha + "). Mögöttes játékfa elhagyása.");
                    break; 
                }
            }
            return minEval;
        }
    }
}
