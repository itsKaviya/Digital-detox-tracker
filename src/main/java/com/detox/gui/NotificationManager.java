package com.detox.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.PopupMenu;
import java.awt.MenuItem;
import java.awt.TrayIcon;
import java.awt.SystemTray;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * Utility class to manage a system tray icon and display notifications.
 * The icon is created once at application startup and can be used from any
 * part of the code base (e.g., AppTracker) to show alerts when limits are
 * exceeded.
 */
public class NotificationManager {
    private static TrayIcon trayIcon;

    /** Initializes the system tray icon. Must be called after the UI theme is set.
     *  If the OS does not support a system tray, this method silently does nothing.
     */
    public static void init() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray not supported on this platform.");
            return;
        }
        try {
            // Load a small PNG icon that lives in the resources folder.
            // If the resource cannot be found we fall back to a generated image.
            Image image = Toolkit.getDefaultToolkit().getImage(NotificationManager.class.getResource("/detox_tray.png"));
            if (image == null) {
                // create a simple fallback image (green leaf shape)
                int size = 16;
                BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(0x6C63FF));
                g.fillOval(0, 0, size, size);
                g.dispose();
                image = img;
            }
            PopupMenu popup = new PopupMenu();
            MenuItem exit = new MenuItem("Exit");
            exit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            popup.add(exit);
            trayIcon = new TrayIcon(image, "Digital Detox Tracker", popup);
            trayIcon.setImageAutoSize(true);
            SystemTray.getSystemTray().add(trayIcon);
        } catch (Exception e) {
            System.err.println("Failed to initialise system tray: " + e.getMessage());
        }
    }

    /** Displays a native notification balloon.
     *  @param title   short title for the notification
     *  @param message descriptive message body
     *  @param type    the MessageType (INFO, WARNING, ERROR)
     */
    public static void showNotification(String title, String message, TrayIcon.MessageType type) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, type);
        }
    }
}
