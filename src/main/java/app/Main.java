package app;

import javax.swing.*;

public class Main {
    static void main() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception e){

        }
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });

        Database.getConnection();
    }
}
