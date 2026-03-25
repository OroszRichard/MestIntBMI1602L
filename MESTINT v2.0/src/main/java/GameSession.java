import java.util.Scanner;

/**
 * A konkrét játékmenetet vezérlő osztály.
 * Bekéri a játékos nevét, betölti az esetleges mentett táblát, és irányítja a játékhurkot.
 */
public class GameSession {
    private final DatabaseManager db;
    private Connect4Board board;
    private final Connect4AI ai;
    private final Scanner scanner;

    private String playerName;
    private final char humanPlayer = 'Y';
    private final char aiPlayer = 'R';
    private final int ROWS = 6;
    private final int COLS = 7;

    public GameSession() {
        this.db = new DatabaseManager();
        this.ai = new Connect4AI(8); // AI erősségének beállítása (8 mélység)
        this.scanner = new Scanner(System.in);
    }

    /**
     * Elindítja a játékot. Inicializál vagy betölt egy már futó állapotot.
     */
    public void start() {
        System.out.println("====== Connect4 AI Játék ======");
        System.out.print("Kérlek, add meg a játékosneved: ");
        playerName = scanner.nextLine().trim();

        // Ellenőrzés az adatbázisban, hogy van-e elkezdett játéka az illetőnek
        char[][] savedState = db.loadUnfinishedGame(playerName, ROWS, COLS);
        if (savedState != null) {
            System.out.println("\n[RENDSZER] Találtam egy félbehagyott meccset! Visszatöltés...");
            board = new Connect4Board(savedState);
        } else {
            System.out.println("\n[RENDSZER] Új meccs indítása...");
            board = new Connect4Board(createEmptyBoard());
        }

        playLoop();
    }

    /**
     * A tényleges játék iteratív lépéseit (játékos -> AI -> játékos) végző hurok.
     */
    private void playLoop() {
        boolean gameRunning = true;

        while (gameRunning) {
            printBoard();

            // 1. Emberi játékos köre
            int col = -1;
            while (true) {
                System.out.print(playerName + " (Jel: 'Y'), hanyadik oszlopba lépsz? (0-6): ");
                if (scanner.hasNextInt()) {
                    col = scanner.nextInt();
                    // Lépés érvényességének tesztelése
                    if (col >= 0 && col < COLS && board.getNextOpenRow(col) != -1) {
                        break;
                    }
                } else {
                    scanner.next(); // Hibás nem-szám input eldobása
                }
                System.out.println("[HIBA] Érvénytelen lépés vagy tele az oszlop! Próbáld újra.");
            }

            int row = board.getNextOpenRow(col);
            board.dropPiece(row, col, humanPlayer);

            // Játék vége check (emberi lépés után)
            if (handleEndGame()) {
                break;
            }

            // Állás kimentése, ha az ember jött, hogy mindig aktuális legyen
            db.saveGameState(playerName, extractRawBoard());

            // 2. AI játékos köre
            System.out.println("Az AI (Jel: 'R') gondolkodik...");
            int aiCol = ai.getBestColumn(extractRawBoard());
            
            if (aiCol != -1) {
                int aiRow = board.getNextOpenRow(aiCol);
                board.dropPiece(aiRow, aiCol, aiPlayer);
                System.out.println("Az AI lépett: " + aiCol + ". oszlop");
            }

            // Játék vége check (AI lépés után)
            if (handleEndGame()) {
                break;
            }

            // Állás kimentése az AI lépését követően
            db.saveGameState(playerName, extractRawBoard());
        }
        
        System.out.println("A játék véget ért. Köszönöm, hogy játszottál!");
    }

    /**
     * Leellenőrzi és kezeli a játék végét (nyert valaki, vagy döntetlen lett).
     * @return true, ha a játéknak vége.
     */
    private boolean handleEndGame() {
        if (board.checkWin(humanPlayer)) {
            printBoard();
            System.out.println("\n*** Győztél, " + playerName + "! Szép munka! ***");
            db.saveResult(playerName, "Győzelem");
            db.deleteGameState(playerName); // Állás már nem félbehagyott, törlés
            return true;
        } else if (board.checkWin(aiPlayer)) {
            printBoard();
            System.out.println("\n*** Az AI nyert! ***");
            db.saveResult(playerName, "Vereség");
            db.deleteGameState(playerName); // Törlés
            return true;
        } else if (board.getValidLocations().isEmpty()) {
            printBoard();
            System.out.println("\n*** Döntetlen! A tábla betelt. ***");
            db.saveResult(playerName, "Döntetlen");
            db.deleteGameState(playerName); // Törlés
            return true;
        }
        return false;
    }

    /**
     * Üres táblát generál array-ből inicializáláshoz.
     */
    private char[][] createEmptyBoard() {
        char[][] empty = new char[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                empty[r][c] = ' ';
            }
        }
        return empty;
    }

    /**
     * Kilapítja a 'Connect4Board' objektumból a nyers char[][] adatot az AI számára és a mentéshez.
     */
    private char[][] extractRawBoard() {
        char[][] raw = new char[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                raw[r][c] = board.getCell(r, c);
            }
        }
        return raw;
    }

    /**
     * Kiírja a tábla grafikus állapotát konzolra.
     */
    private void printBoard() {
        System.out.println("\n 0 1 2 3 4 5 6");
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                System.out.print("|" + board.getCell(r, c));
            }
            System.out.println("|");
        }
        System.out.println("---------------");
    }

    // Használati példa közvetlen indításhoz
    public static void main(String[] args) {
        new GameSession().start();
    }
}
