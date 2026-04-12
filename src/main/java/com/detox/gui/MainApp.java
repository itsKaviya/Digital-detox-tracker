package com.detox.gui;

import com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme;
import com.detox.DatabaseManager;

import javax.swing.*;
import java.awt.*;

/**
 * Application entry point.
 * Initialises FlatLaf theme, creates the main window, and shows the login screen.
 */
public class MainApp {

    public static void main(String[] args) {
        // Install FlatDracula theme BEFORE any Swing component is created
        FlatDraculaIJTheme.setup();

        // Set global UI defaults for a consistent premium look
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));

        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseManager db = DatabaseManager.getInstance();
                MainFrame frame = new MainFrame(db);
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Failed to connect to database:\n" + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
