/**
 * A Connect4 tábla heurisztikus értékelését végző, tokozott osztály.
 */
public class BoardEvaluator {

    /**
     * Értékeli a tábla jelenlegi állapotát az AI szemszögéből.
     * 
     * @param board A vizsgálandó tábla objektum.
     * @param aiPlayer Az AI azonosítója.
     * @param humanPlayer Az emberi játékos azonosítója.
     * @return A tábla heurisztikus pontszáma.
     */
    public int evaluate(Connect4Board board, char aiPlayer, char humanPlayer) {
        int score = 0;
        int rows = board.getRows();
        int cols = board.getCols();

        // A középső oszlop preferálása
        int centerCol = cols / 2;
        int centerCount = 0;
        for (int r = 0; r < rows; r++) {
            if (board.getCell(r, centerCol) == aiPlayer) {
                centerCount++;
            }
        }
        score += centerCount * 3;

        // Vízszintes ablakok vizsgálata
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 3; c++) {
                char[] window = {board.getCell(r, c), board.getCell(r, c+1), board.getCell(r, c+2), board.getCell(r, c+3)};
                score += evaluateWindow(window, aiPlayer, humanPlayer, board.getEmptySlot());
            }
        }

        // Függőleges ablakok vizsgálata
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows - 3; r++) {
                char[] window = {board.getCell(r, c), board.getCell(r+1, c), board.getCell(r+2, c), board.getCell(r+3, c)};
                score += evaluateWindow(window, aiPlayer, humanPlayer, board.getEmptySlot());
            }
        }

        // Pozitív átlók vizsgálata
        for (int r = 0; r < rows - 3; r++) {
            for (int c = 0; c < cols - 3; c++) {
                char[] window = {board.getCell(r, c), board.getCell(r+1, c+1), board.getCell(r+2, c+2), board.getCell(r+3, c+3)};
                score += evaluateWindow(window, aiPlayer, humanPlayer, board.getEmptySlot());
            }
        }

        // Negatív átlók vizsgálata
        for (int r = 0; r < rows - 3; r++) {
            for (int c = 0; c < cols - 3; c++) {
                char[] window = {board.getCell(r+3, c), board.getCell(r+2, c+1), board.getCell(r+1, c+2), board.getCell(r, c+3)};
                score += evaluateWindow(window, aiPlayer, humanPlayer, board.getEmptySlot());
            }
        }

        return score;
    }

    /**
     * Belső segédmetódus egy "ablak" (4 hosszú sorozat) kiértékeléséhez.
     */
    private int evaluateWindow(char[] window, char aiPiece, char humanPiece, char emptySlot) {
        int score = 0;
        int pieceCount = 0;
        int emptyCount = 0;
        int oppCount = 0;

        for (char cell : window) {
            if (cell == aiPiece) pieceCount++;
            else if (cell == emptySlot) emptyCount++;
            else if (cell == humanPiece) oppCount++;
        }

        // Saját győzelmi esélyek pontozása
        if (pieceCount == 4) {
            score += 100; // Győzelem
        } else if (pieceCount == 3 && emptyCount == 1) {
            score += 5; // Majdnem kész
        } else if (pieceCount == 2 && emptyCount == 2) {
            score += 2; // Épülőfélben
        }

        // Az ellenfél veszélyességének blokkolása
        if (oppCount == 3 && emptyCount == 1) {
            score -= 4; // Nagy mínusz, ha nem akadályozzuk meg a nyerését
        }

        return score;
    }
}
