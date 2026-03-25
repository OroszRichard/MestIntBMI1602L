import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardEvaluatorTest {
    
    private char[][] getEmptyGrid() {
        char[][] grid = new char[6][7];
        for(int r = 0; r < 6; r++) for(int c = 0; c < 7; c++) grid[r][c] = ' ';
        return grid;
    }

    @Test
    public void testCenterColumnPreference() {
        BoardEvaluator eval = new BoardEvaluator();
        Connect4Board board = new Connect4Board(getEmptyGrid());
        
        // AI in center column (+3 per piece)
        board.dropPiece(5, 3, 'R');
        int score = eval.evaluate(board, 'R', 'Y');
        
        assertTrue(score >= 3, "A középső oszlopba rakásért pont jár.");
    }

    @Test
    public void testHeuristicScores() {
        BoardEvaluator eval = new BoardEvaluator();
        
        // 4 in a row -> +100
        Connect4Board board1 = new Connect4Board(getEmptyGrid());
        board1.dropPiece(5, 0, 'R'); board1.dropPiece(5, 1, 'R');
        board1.dropPiece(5, 2, 'R'); board1.dropPiece(5, 3, 'R');
        assertTrue(eval.evaluate(board1, 'R', 'Y') >= 100);

        // 3 in a row with 1 empty -> +5
        Connect4Board board2 = new Connect4Board(getEmptyGrid());
        board2.dropPiece(5, 0, 'R'); board2.dropPiece(5, 1, 'R');
        board2.dropPiece(5, 2, 'R'); 
        assertTrue(eval.evaluate(board2, 'R', 'Y') >= 5);

        // 2 in a row with 2 empty -> +2
        Connect4Board board3 = new Connect4Board(getEmptyGrid());
        board3.dropPiece(5, 0, 'R'); board3.dropPiece(5, 1, 'R');
        assertTrue(eval.evaluate(board3, 'R', 'Y') >= 2);
    }

    @Test
    public void testOpponentBlockPenalty() {
        BoardEvaluator eval = new BoardEvaluator();
        Connect4Board board = new Connect4Board(getEmptyGrid());
        
        // Ellenfélnek 3 van egymás mellett
        board.dropPiece(5, 0, 'Y'); 
        board.dropPiece(5, 1, 'Y'); 
        board.dropPiece(5, 2, 'Y');
        
        int score = eval.evaluate(board, 'R', 'Y');
        assertTrue(score <= -4, "Mínusz pont, ha az ellenfél majdnem nyer.");
    }
}
