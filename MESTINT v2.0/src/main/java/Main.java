import javax.swing.SwingUtilities;

/**
 * A Connect4 Java alkalmazás hivatalos belépési pontja.
 * Ez az osztály van beállítva a pom.xml-ben Main-Class-ként is az indításhoz.
 */
public class Main {
    
    public static void main(String[] args) {
        // A Swing grafikus felületek biztonságos, Java ajánlások szerinti indítása az eseménykezelő szálon (EDT)
        SwingUtilities.invokeLater(() -> {
            Connect4GUI app = new Connect4GUI();
            app.start();
        });
    }
}
