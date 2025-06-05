package edu.iuc;

import edu.iuc.client.MainMenuFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                MainMenuFrame mainFrame = new MainMenuFrame();
                mainFrame.setVisible(true);
                
            } catch (Exception e) {
                System.err.println("Uygulama başlatılırken hata: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}