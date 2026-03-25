import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * A Connect4 játék Swing alapú grafikus felülete (GUI).
 * Szorosan integrálódik az adatbázis-kezelővel és a Mesterséges Intelligenciával.
 */
public class Connect4GUI extends JFrame {

    private final int ROWS = 6;
    private final int COLS = 7;
    private final char humanPlayer = 'Y'; // Sárga
    private final char aiPlayer = 'R';    // Piros
    
    // Háttér logikai elemek
    private Connect4Board board;
    private final Connect4AI ai;
    private final DatabaseManager db;
    private String playerName;
    
    // GUI komponensek
    private final BoardPanel boardPanel;
    private final JLabel statusLabel;
    private final JButton[] colButtons;
    private final JButton btnNewGame;
    
    private boolean isGameActive = false;

    public Connect4GUI() {
        super("Connect4 AI - Grafikus Felület");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Inicializáljuk a háttér logikát
        this.db = new DatabaseManager();
        this.ai = new Connect4AI(8); // AI erősség: 8 lépés előre

        // Felső panel a gomboknak (Oszlopválasztó)
        JPanel topPanel = new JPanel(new GridLayout(1, COLS));
        colButtons = new JButton[COLS];
        for (int c = 0; c < COLS; c++) {
            JButton btn = new JButton("V" + (c + 1));
            final int colIndex = c;
            btn.setFont(new Font("Arial", Font.BOLD, 16));
            btn.setFocusPainted(false);
            btn.addActionListener(e -> handleHumanMove(colIndex));
            colButtons[c] = btn;
            topPanel.add(btn);
        }
        add(topPanel, BorderLayout.NORTH);

        // Középső panel a táblának
        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // Alsó panel státuszsávval és új játék gombbal
        JPanel bottomPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Üdvözöllek! Kérlek várj...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        btnNewGame = new JButton("Új Játék");
        btnNewGame.setFont(new Font("Arial", Font.BOLD, 14));
        btnNewGame.addActionListener(e -> startNewGame(true));
        
        JButton btnHighscore = new JButton("Toplista");
        btnHighscore.setFont(new Font("Arial", Font.BOLD, 14));
        btnHighscore.addActionListener(e -> showHighscores());

        JPanel buttonBox = new JPanel();
        buttonBox.add(btnHighscore);
        buttonBox.add(btnNewGame);
        
        bottomPanel.add(buttonBox, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Elindítja a felületet, bekéri a nevet és inicializálja az állapotot.
     */
    public void start() {
        setVisible(true);
        
        playerName = JOptionPane.showInputDialog(this, "Add meg a játékosneved:", "Bejelentkezés", JOptionPane.QUESTION_MESSAGE);
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Vendég";
        }

        // Félbemaradt meccs betöltése
        char[][] savedState = db.loadUnfinishedGame(playerName, ROWS, COLS);
        if (savedState != null) {
            int response = JOptionPane.showConfirmDialog(this, 
                "Találtam egy félbehagyott meccset! Szeretnéd folytatni?", 
                "Mentés betöltése", JOptionPane.YES_NO_OPTION);
            
            if (response == JOptionPane.YES_OPTION) {
                board = new Connect4Board(savedState);
                statusLabel.setText(playerName + " (Sárga) következik!");
                isGameActive = true;
                refreshGUI();
                return;
            }
        }
        
        // Ha nincs mentés, vagy elutasította a folytatást
        startNewGame(false);
    }

    /**
     * Tiszta lappal új játékot indít.
     */
    private void startNewGame(boolean clearDb) {
        if (clearDb && playerName != null) {
            db.deleteGameState(playerName);
        }
        
        char[][] emptyGrid = new char[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                emptyGrid[r][c] = ' ';
            }
        }
        board = new Connect4Board(emptyGrid);
        isGameActive = true;
        statusLabel.setText("Új játék. " + playerName + " (Sárga) következik!");
        refreshGUI();
    }

    /**
     * Kezeli az emberi játékos lépését.
     */
    private void handleHumanMove(int colIndex) {
        if (!isGameActive) return;

        int row = board.getNextOpenRow(colIndex);
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Ez az oszlop tele van!", "Hiba", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lépés megjátszása
        board.dropPiece(row, colIndex, humanPlayer);
        refreshGUI();

        if (checkEndGame(humanPlayer)) {
            return;
        }

        db.saveGameState(playerName, extractRawBoard());
        
        // AI gondolkodik (SwingWorker bevonásával, hogy ne fagyjon le a GUI)
        statusLabel.setText("Az AI (Piros) gondolkodik...");
        disableButtons();
        
        SwingWorker<Integer, Void> aiWorker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                return ai.getBestColumn(extractRawBoard());
            }

            @Override
            protected void done() {
                try {
                    int aiCol = get();
                    if (aiCol != -1) {
                        int aiRow = board.getNextOpenRow(aiCol);
                        board.dropPiece(aiRow, aiCol, aiPlayer);
                        refreshGUI();

                        if (!checkEndGame(aiPlayer)) {
                            db.saveGameState(playerName, extractRawBoard());
                            statusLabel.setText(playerName + " (Sárga) következik!");
                            enableValidButtons();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        aiWorker.execute();
    }

    /**
     * Ellenőrzi a végállapotot egy adott lépést követően. Menti az eredményt az DB-be.
     */
    private boolean checkEndGame(char piece) {
        boolean gameEnded = false;
        
        if (board.checkWin(piece)) {
            if (piece == humanPlayer) {
                statusLabel.setText("Gratulálok, " + playerName + "! Győztél!");
                db.saveResult(playerName, "Győzelem");
                JOptionPane.showMessageDialog(this, "Nyertél!", "Játék vége", JOptionPane.INFORMATION_MESSAGE);
            } else {
                statusLabel.setText("Az AI nyert! :(");
                db.saveResult(playerName, "Vereség");
                JOptionPane.showMessageDialog(this, "Sajnos az AI nyert. Próbáld újra! :(", "Játék vége", JOptionPane.ERROR_MESSAGE);
            }
            gameEnded = true;
        } else if (board.getValidLocations().isEmpty()) {
            statusLabel.setText("Döntetlen! A tábla megtelt.");
            db.saveResult(playerName, "Döntetlen");
            JOptionPane.showMessageDialog(this, "Döntetlen eredmény!", "Játék vége", JOptionPane.INFORMATION_MESSAGE);
            gameEnded = true;
        }

        if (gameEnded) {
            isGameActive = false;
            db.deleteGameState(playerName);
            disableButtons();
            return true;
        }
        
        return false;
    }

    private void showHighscores() {
        String scores = db.getHighscoresFormatted();
        JOptionPane.showMessageDialog(this, scores, "Dicsőségtábla (Toplista)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshGUI() {
        boardPanel.setBoard(board);
        if (isGameActive) {
            enableValidButtons();
        }
    }

    private void disableButtons() {
        for (JButton btn : colButtons) {
            btn.setEnabled(false);
        }
    }

    private void enableValidButtons() {
        List<Integer> validCols = board.getValidLocations();
        for (int c = 0; c < COLS; c++) {
            colButtons[c].setEnabled(validCols.contains(c));
        }
    }

    private char[][] extractRawBoard() {
        char[][] raw = new char[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                raw[r][c] = board.getCell(r, c);
            }
        }
        return raw;
    }

    public static void main(String[] args) {
        // Swing GUI indítása dedikált szálon
        SwingUtilities.invokeLater(() -> {
            Connect4GUI gui = new Connect4GUI();
            gui.start();
        });
    }

    /**
     * Belső osztály a tábla kirajzolásához (Egyedi grafika)
     */
    private class BoardPanel extends JPanel {
        private Connect4Board internalBoard;

        public void setBoard(Connect4Board board) {
            this.internalBoard = board;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Háttér kékre festése
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(20, 50, 200)); // Szép sötétkék logikai tábla szín
            g2d.fillRect(0, 0, getWidth(), getHeight());

            if (internalBoard == null) return;

            int rows = internalBoard.getRows();
            int cols = internalBoard.getCols();
            
            // Cellák méretének dinamikus számítása az ablak átméretezése esetén
            int cellWidth = getWidth() / cols;
            int cellHeight = getHeight() / rows;
            // A lyukak körüli térköz (margó)
            int padding = 10;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    char piece = internalBoard.getCell(r, c);
                    if (piece == 'R') {
                        // AI = Piros (Sugárirányú színátmenettel még szebb lenne, most sima piros)
                        g2d.setColor(Color.RED);
                    } else if (piece == 'Y') {
                        // Játékos = Sárga
                        g2d.setColor(Color.YELLOW);
                    } else {
                        // Üres mező = Fehér (vagy szürke lyuk)
                        g2d.setColor(new Color(230, 230, 230));
                    }
                    
                    // Korongok kirajzolása
                    g2d.fillOval(c * cellWidth + padding, r * cellHeight + padding, 
                                 cellWidth - 2 * padding, cellHeight - 2 * padding);
                }
            }
        }
    }
}
