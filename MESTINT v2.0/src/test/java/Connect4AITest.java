import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A Connect4 AI belső metódusait maximalizált áglefedettséggel (Branch Coverage) vizsgáló tesztosztály.
 */
public class Connect4AITest {
    private Connect4AI ai;

    @BeforeEach
    public void setUp() {
        ai = new Connect4AI(4); 
    }

    private char[][] createEmptyBoard() {
        char[][] grid = new char[6][7];
        for (int r = 0; r < 6; r++) for (int c = 0; c < 7; c++) grid[r][c] = ' ';
        return grid;
    }

    @Test
    public void testAITakesWinningMove() {
        char[][] grid = createEmptyBoard();
        grid[5][0] = 'R'; grid[5][1] = 'R'; grid[5][2] = 'R';
        assertEquals(3, ai.getBestColumn(grid));
    }

    @Test
    public void testAIBlocksOpponentWin() {
        char[][] grid = createEmptyBoard();
        grid[5][0] = 'Y'; grid[4][0] = 'Y'; grid[3][0] = 'Y';
        assertEquals(0, ai.getBestColumn(grid));
    }
    
    @Test
    public void testAIPrefersCenterCol() {
        char[][] grid = createEmptyBoard();
        assertEquals(3, ai.getBestColumn(grid));
    }

    @Test
    public void testMinimaxDrawConditionAndFallback() {
        char[][] grid = new char[6][7];
        // Olyan tábla generálása, ahol garantált a döntetlen az utolsó lépéssel
        for (int r = 0; r < 6; r++) for (int c = 0; c < 7; c++) grid[r][c] = (c % 2 == 0) ? 'Y' : 'R';
        grid[0][0] = ' '; // Csak ez az 1 hely üres
        
        int col = ai.getBestColumn(grid);
        assertEquals(0, col, "Utolsó hely az egyetlen érvényes lépés");
    }
    
    @Test
    public void testFullBoardReturnsInvalid() {
        char[][] grid = new char[6][7];
        for (int r = 0; r < 6; r++) for (int c = 0; c < 7; c++) grid[r][c] = 'X';
        
        int col = ai.getBestColumn(grid);
        assertEquals(-1, col, "Tele táblánál -1-et kell visszaadnia");
    }
}
