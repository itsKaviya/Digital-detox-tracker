package com.detox.gui;

import com.detox.DatabaseManager;
import com.detox.User;

import javax.swing.*;
import java.awt.*;

/**
 * The main application window. Uses a CardLayout to switch between
 * the Login screen and the Dashboard.
 */
public class MainFrame extends JFrame {

    private static final int WIDTH  = 1100;
    private static final int HEIGHT = 720;

    private final DatabaseManager db;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel     = new JPanel(cardLayout);

    private LoginPanel     loginPanel;
    private DashboardPanel dashboardPanel;

    public MainFrame(DatabaseManager db) {
        this.db = db;
        initWindow();
        buildCards();
    }

    private void initWindow() {
        setTitle("Digital Detox Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(860, 600));
        setLocationRelativeTo(null); // centre on screen

        // Custom app icon (leaf emoji rendered as image)
        setIconImage(createAppIcon());

        setContentPane(cardPanel);
    }

    private void buildCards() {
        loginPanel = new LoginPanel(db, this);
        cardPanel.add(loginPanel, "LOGIN");
        cardLayout.show(cardPanel, "LOGIN");
    }

    /** Called by LoginPanel after a successful login/register. */
    public void showDashboard(User user) {
        if (dashboardPanel != null) {
            cardPanel.remove(dashboardPanel);
        }
        dashboardPanel = new DashboardPanel(db, user, this);
        cardPanel.add(dashboardPanel, "DASHBOARD");
        cardLayout.show(cardPanel, "DASHBOARD");
        revalidate();
    }

    /** Called by DashboardPanel when the user logs out. */
    public void showLogin() {
        dashboardPanel = null;
        cardLayout.show(cardPanel, "LOGIN");
        loginPanel.reset();
    }

    /** Creates a simple coloured square as app icon. */
    private Image createAppIcon() {
        int size = 64;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Gradient background
        GradientPaint gp = new GradientPaint(0, 0, new Color(0x6C63FF),
                                             size, size, new Color(0x3EC6C6));
        g.setPaint(gp);
        g.fillRoundRect(0, 0, size, size, 16, 16);
        // Letter
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        String letter = "D";
        int x = (size - fm.stringWidth(letter)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(letter, x, y);
        g.dispose();
        return img;
    }
}
