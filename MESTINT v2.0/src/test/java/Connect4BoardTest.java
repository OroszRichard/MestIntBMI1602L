import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class Connect4BoardTest {

    private char[][] getEmptyGrid() {
        char[][] grid = new char[6][7];
        for (int r = 0; r < 6; r++) for (int c = 0; c < 7; c++) grid[r][c] = ' ';
        return grid;
    }

    @Test
    public void testBoardInitialization() {
        Connect4Board board = new Connect4Board(getEmptyGrid());

        assertEquals(6, board.getRows());
        assertEquals(7, board.getCols());
        assertEquals(' ', board.getEmptySlot());
        assertEquals(' ', board.getCell(0, 0));
    }

    @Test
    public void testDropAndUndoMove() {
        Connect4Board board = new Connect4Board(getEmptyGrid());

        int row = board.getNextOpenRow(0);
        assertEquals(5, row);
        
        board.dropPiece(row, 0, 'R');
        assertEquals('R', board.getCell(5, 0));

        int nextRow = board.getNextOpenRow(0);
        assertEquals(4, nextRow);

        board.undoMove(5, 0);
        assertEquals(' ', board.getCell(5, 0));
        assertEquals(5, board.getNextOpenRow(0)); // Újra üres lett az alja
    }

    @Test
    public void testValidLocations() {
        Connect4Board board = new Connect4Board(getEmptyGrid());

        List<Integer> valid = board.getValidLocations();
        assertEquals(7, valid.size(), "Minden oszlop érvényes kezdetben");

        // Egy teljes oszlop feltöltése
        for (int r = 0; r < 6; r++) {
            board.dropPiece(r, 2, 'R');
        }

        valid = board.getValidLocations();
        assertEquals(6, valid.size(), "A 2. oszlop betelt, csak 6 lépés lehetséges");
        assertFalse(valid.contains(2), "A beállított oszlop nem érvényes");
        assertEquals(-1, board.getNextOpenRow(2), "A betelt oszlopban nincs nyitott sor");
    }

    @Test
    public void testHorizontalWin() {
        Connect4Board board = new Connect4Board(getEmptyGrid());
        assertFalse(board.checkWin('Y'));

        board.dropPiece(5, 0, 'Y');
        board.dropPiece(5, 1, 'Y');
        board.dropPiece(5, 2, 'Y');
        board.dropPiece(5, 3, 'Y');

        assertTrue(board.checkWin('Y'), "Vízszintes győzelem");
        assertFalse(board.checkWin('R'));
        assertTrue(board.isTerminalNode('R', 'Y'));
    }

    @Test
    public void testVerticalWin() {
        Connect4Board board = new Connect4Board(getEmptyGrid());

        board.dropPiece(5, 0, 'R');
        board.dropPiece(4, 0, 'R');
        board.dropPiece(3, 0, 'R');
        board.dropPiece(2, 0, 'R');

        assertTrue(board.checkWin('R'), "Függőleges győzelem");
        assertFalse(board.checkWin('Y'));
    }

    @Test
    public void testPositiveDiagonalWin() {
        Connect4Board board = new Connect4Board(getEmptyGrid());

        board.dropPiece(5, 0, 'R');
        board.dropPiece(4, 1, 'R');
        board.dropPiece(3, 2, 'R');
        board.dropPiece(2, 3, 'R');

        assertTrue(board.checkWin('R'), "Pozitív átlós győzelem");
    }

    @Test
    public void testNegativeDiagonalWin() {
        Connect4Board board = new Connect4Board(getEmptyGrid());

        board.dropPiece(2, 0, 'Y');
        board.dropPiece(3, 1, 'Y');
        board.dropPiece(4, 2, 'Y');
        board.dropPiece(5, 3, 'Y');

        assertTrue(board.checkWin('Y'), "Negatív átlós győzelem");
    }

    @Test
    public void testDrawTerminalState() {
        char[][] grid = new char[6][7];
        // Olyan tábla kitöltés, ahol garantáltan nincs nyertes
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                grid[r][c] = (c % 2 == 0) ? 'X' : 'Z';
            }
        }
        Connect4Board board = new Connect4Board(grid);

        assertTrue(board.getValidLocations().isEmpty(), "A tábla betelt");
        assertTrue(board.isTerminalNode('R', 'Y'), "Végállapot érzékelése döntetlennél");
        assertFalse(board.checkWin('R'));
        assertFalse(board.checkWin('Y'));
    }

    @Test
    public void testPartialWinsForBranchCoverage() {
        // Horizontal
        Connect4Board b1 = new Connect4Board(getEmptyGrid());
        b1.dropPiece(0, 0, 'Y'); b1.dropPiece(0, 1, 'Y'); b1.dropPiece(0, 2, 'Y'); 
        assertFalse(b1.checkWin('Y'));

        // Vertical
        Connect4Board b2 = new Connect4Board(getEmptyGrid());
        b2.dropPiece(0, 0, 'Y'); b2.dropPiece(1, 0, 'Y'); b2.dropPiece(2, 0, 'Y');
        assertFalse(b2.checkWin('Y'));

        // Positive Diagonal
        Connect4Board b3 = new Connect4Board(getEmptyGrid());
        b3.dropPiece(0, 0, 'Y'); b3.dropPiece(1, 1, 'Y'); b3.dropPiece(2, 2, 'Y');
        assertFalse(b3.checkWin('Y'));

        // Negative Diagonal
        Connect4Board b4 = new Connect4Board(getEmptyGrid());
        b4.dropPiece(3, 0, 'Y'); b4.dropPiece(2, 1, 'Y'); b4.dropPiece(1, 2, 'Y');
        assertFalse(b4.checkWin('Y'));
    }
}
