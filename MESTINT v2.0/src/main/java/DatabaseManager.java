import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Adatbázis-kezelő osztály a Connect4 játékhoz (SQLite).
 * Felelős az eredmények és a félbemaradt játékok állapotainak perzisztens tárolásáért.
 */
public class DatabaseManager {
    // Adatbázis fájl helyi megadása (alkalmazási könyvtárba menti)
    private static final String DB_URL = "jdbc:sqlite:connect4.db";

    public DatabaseManager() {
        initDatabase();
    }

    /**
     * Végrehajtja a táblák inicializálását az első indításkor.
     */
    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Eredmények táblája: Játékos neve, kimenetel, és egy időbélyeg
            String createResultsTable = "CREATE TABLE IF NOT EXISTS results (" +
                                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                        "player_name TEXT NOT NULL," +
                                        "result TEXT NOT NULL," +
                                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                                        ");";
                                        
            // Mentett játékállások táblája játékos név szerinti kereséshez
            String createGamesTable = "CREATE TABLE IF NOT EXISTS saved_games (" +
                                      "player_name TEXT PRIMARY KEY," +
                                      "board_state TEXT NOT NULL" +
                                      ");";

            stmt.execute(createResultsTable);
            stmt.execute(createGamesTable);
        } catch (SQLException e) {
            System.err.println("Hiba az adatbázis inicializálásakor: " + e.getMessage());
        }
    }

    /**
     * Eltárol egy végleges meccseredményt.
     * 
     * @param playerName A játékos neve.
     * @param result Az eredmény (pl. "Győzelem", "Vereség", "Döntetlen").
     */
    public void saveResult(String playerName, String result) {
        String sql = "INSERT INTO results(player_name, result) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setString(2, result);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Hiba az eredmény mentésekor: " + e.getMessage());
        }
    }

    /**
     * Menti az aktuális játékállást a megadott játékoshoz. 
     * Felülírja az előző mentést (INSERT OR REPLACE).
     */
    public void saveGameState(String playerName, char[][] board) {
        String sql = "INSERT OR REPLACE INTO saved_games (player_name, board_state) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setString(2, boardToString(board));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Hiba az állás mentésekor: " + e.getMessage());
        }
    }

    /**
     * Törli a mentett játékállást az adatbázisból, ha a játéknak (szabályosan) vége.
     */
    public void deleteGameState(String playerName) {
        String sql = "DELETE FROM saved_games WHERE player_name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Hiba a mentett állás törlésekor: " + e.getMessage());
        }
    }

    /**
     * Betölti egy játékos megkezdett játékát, ha talál.
     * 
     * @return A betöltött tábla char[][] formában, vagy null, ha a játékosnak nincs mentett játéka.
     */
    public char[][] loadUnfinishedGame(String playerName, int rows, int cols) {
        String sql = "SELECT board_state FROM saved_games WHERE player_name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String stateStr = rs.getString("board_state");
                    return stringToBoard(stateStr, rows, cols);
                }
            }
        } catch (SQLException e) {
            System.err.println("Hiba a mentett állás betöltésekor: " + e.getMessage());
        }
        return null; // Ha nincs meccs elmentve
    }

    /**
     * Stringgé alakítja a char[][] táblát az adatbázisban tároláshoz.
     */
    private String boardToString(char[][] board) {
        StringBuilder sb = new StringBuilder();
        for (char[] row : board) {
            for (char cell : row) {
                sb.append(cell);
            }
        }
        return sb.toString();
    }

    /**
     * Stringből visszaalakítja a szabályos char[][] táblává.
     */
    private char[][] stringToBoard(String stateStr, int rows, int cols) {
        char[][] board = new char[rows][cols];
        int index = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (index < stateStr.length()) {
                    board[r][c] = stateStr.charAt(index++);
                } else {
                    board[r][c] = ' '; // Biztonsági kitöltés
                }
            }
        }
        return board;
    }

    /**
     * Visszaadja a formázott toplistát a játékosok győzelmeivel és az AI globális statisztikájával.
     */
    public String getHighscoresFormatted() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DICSŐSÉGTÁBLA (Toplista) ===\n\n");
        String sql = "SELECT player_name, result, COUNT(*) as count FROM results GROUP BY player_name, result ORDER BY player_name";
        int aiWins = 0;
        int aiLosses = 0;
        int aiDraws = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            String currentPlayer = "";
            int wins = 0;
            int losses = 0;
            int draws = 0;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String player = rs.getString("player_name");
                String result = rs.getString("result");
                int count = rs.getInt("count");

                if (!player.equals(currentPlayer)) {
                    if (!currentPlayer.isEmpty()) {
                        sb.append(currentPlayer).append(" -> Győzelem: ").append(wins)
                          .append(" | Vereség: ").append(losses)
                          .append(" | Döntetlen: ").append(draws).append("\n");
                    }
                    currentPlayer = player;
                    wins = 0; losses = 0; draws = 0;
                }

                if ("Győzelem".equals(result)) {
                    wins = count;
                    aiLosses += count; // Ha a játékos nyer, az AI veszít
                } else if ("Vereség".equals(result)) {
                    losses = count;
                    aiWins += count; // Ha a játékos veszít, az AI nyer
                } else if ("Döntetlen".equals(result)) {
                    draws = count;
                    aiDraws += count;
                }
            }
            if (!currentPlayer.isEmpty()) {
                sb.append(currentPlayer).append(" -> Győzelem: ").append(wins)
                  .append(" | Vereség: ").append(losses)
                  .append(" | Döntetlen: ").append(draws).append("\n");
            }
            if (!hasData) {
                sb.append("Még nincsenek elmentett eredmények.\n");
            }

            sb.append("\n=== AI GLOBÁLIS STATISZTIKÁJA ===\n");
            sb.append("AI összesített győzelme: ").append(aiWins).append("\n");
            sb.append("AI összesített veresége: ").append(aiLosses).append("\n");
            sb.append("AI összesített döntetlenje: ").append(aiDraws).append("\n");

        } catch (SQLException e) {
            sb.append("Hiba az eredmények betöltésekor: ").append(e.getMessage());
        }
        return sb.toString();
    }
}
