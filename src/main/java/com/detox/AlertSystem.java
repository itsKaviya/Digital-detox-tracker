package com.detox;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Alert System Module: Warns users when approaching or exceeding safe limits.
 */
public class AlertSystem {

    public void checkAndAlert(Component parent, ScreenTimeRecord record, User user) {
        int total = record.getTotalTime();
        int limit = user.getDailySafeLimit();

        if (total > limit) {
            JOptionPane.showMessageDialog(parent,
                "⚠️ EXCEEDED SAFE LIMIT!\n\n" +
                "You have used screens for " + total + " minutes today,\n" +
                "which is " + (total - limit) + " minutes over your safe limit.\n\n" +
                "Please consider taking a break immediately.",
                "Digital Detox Alert",
                JOptionPane.WARNING_MESSAGE);
        } else if (total > limit * 0.9) {
            JOptionPane.showMessageDialog(parent,
                "🔔 APPROACHING SAFE LIMIT\n\n" +
                "You are at " + total + " minutes of screen time.\n" +
                "You have only " + (limit - total) + " minutes remaining.\n\n" +
                "Plan your remaining digital activities carefully.",
                "Digital Detox Notification",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
