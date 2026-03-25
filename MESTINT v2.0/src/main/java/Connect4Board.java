import java.util.ArrayList;
import java.util.List;

/**
 * A Connect4 táblát és annak állapotát kezelő, tokozott osztály.
 * Felelős a lépések validálásáért, végrehajtásáért, visszavonásáért, illetve a győzelem ellenőrzéséért.
 */
public class Connect4Board {
    private final char[][] grid;
    private final int rows;
    private final int cols;
    private final char emptySlot = ' ';

    /**
     * Konstruktor, ami a nyers 2D tömb alapján inicializálja az objektumot.
     * Készít egy belső másolatot munka közben, hogy ne írja felül a külső, eredeti táblát.
     */
    public Connect4Board(char[][] initialState) {
        this.rows = initialState.length;
        this.cols = initialState[0].length;
        this.grid = new char[rows][cols];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(initialState[r], 0, this.grid[r], 0, cols);
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public char getCell(int r, int c) {
        return grid[r][c];
    }
    
    public char getEmptySlot() {
        return emptySlot;
    }

    /**
     * Visszaadja azon oszlopok listáját, ahová még lehet érvényes lépést tenni.
     */
    public List<Integer> getValidLocations() {
        List<Integer> validLocations = new ArrayList<>();
        // Feltételezve, hogy a legfelső sor a 0. indexű
        for (int c = 0; c < cols; c++) {
            if (grid[0][c] == emptySlot) {
                validLocations.add(c);
            }
        }
        return validLocations;
    }

    /**
     * Megkeresi, hogy egy adott oszlopban hová esne a következő bedobott korong (alulról felfelé haladva).
     */
    public int getNextOpenRow(int col) {
        for (int r = rows - 1; r >= 0; r--) {
            if (grid[r][col] == emptySlot) {
                return r;
            }
        }
        return -1;
    }

    /**
     * Letesz egy korongot egy adott koordinátára.
     */
    public void dropPiece(int row, int col, char piece) {
        grid[row][col] = piece;
    }

    /**
     * Visszavon egy lépést az adott koordinátáról.
     */
    public void undoMove(int row, int col) {
        grid[row][col] = emptySlot;
    }

    /**
     * Ellenőrzi, hogy a megadott játékosnak kialakult-e a 4 hosszú nyertes sorozata.
     */
    public boolean checkWin(char piece) {
        // Vízszintes
        for (int c = 0; c < cols - 3; c++) {
            for (int r = 0; r < rows; r++) {
                if (grid[r][c] == piece && grid[r][c+1] == piece && grid[r][c+2] == piece && grid[r][c+3] == piece) return true;
            }
        }
        // Függőleges
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows - 3; r++) {
                if (grid[r][c] == piece && grid[r+1][c] == piece && grid[r+2][c] == piece && grid[r+3][c] == piece) return true;
            }
        }
        // Pozitív hajlásszögű átló
        for (int c = 0; c < cols - 3; c++) {
            for (int r = 0; r < rows - 3; r++) {
                if (grid[r][c] == piece && grid[r+1][c+1] == piece && grid[r+2][c+2] == piece && grid[r+3][c+3] == piece) return true;
            }
        }
        // Negatív hajlásszögű átló
        for (int c = 0; c < cols - 3; c++) {
            for (int r = 3; r < rows; r++) {
                if (grid[r][c] == piece && grid[r-1][c+1] == piece && grid[r-2][c+2] == piece && grid[r-3][c+3] == piece) return true;
            }
        }
        return false;
    }

    /**
     * Visszaadja, hogy a játék véget ért-e valamilyen formában.
     */
    public boolean isTerminalNode(char aiPlayer, char humanPlayer) {
        return checkWin(aiPlayer) || checkWin(humanPlayer) || getValidLocations().isEmpty();
    }
}
