package com.detox.gui;

import com.detox.DatabaseManager;
import com.detox.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class SettingsDialog extends JDialog {
    private final DatabaseManager db;
    private final User user;
    
    private JTextField nameField;
    private JSpinner limitSpinner;
    private JSpinner sleepStartSpinner;
    private JSpinner sleepEndSpinner;
    private JSpinner socialLimitSpinner;
    private JSpinner entertainmentLimitSpinner;
    private boolean saved = false;

    private static final Color BG = new Color(0x2B2D3A);
    private static final Color TEXT = new Color(0xEEEEEE);
    private static final Color ACCENT = new Color(0x6C63FF);

    public SettingsDialog(Frame owner, DatabaseManager db, User user) {
        super(owner, "User Settings", true);
        this.db = db;
        this.user = user;

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(24, 24, 24, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nl = new JLabel("Display Name:");
        nl.setForeground(TEXT);
        content.add(nl, gbc);
        gbc.gridx = 1;
        nameField = new JTextField(user.getName(), 15);
        content.add(nameField, gbc);

        // Daily Limit
        gbc.gridx = 0; gbc.gridy++;
        JLabel ll = new JLabel("Daily Limit (min):");
        ll.setForeground(TEXT);
        content.add(ll, gbc);
        gbc.gridx = 1;
        limitSpinner = new JSpinner(new SpinnerNumberModel(user.getDailySafeLimit(), 30, 600, 15));
        content.add(limitSpinner, gbc);

        // Sleep Start
        gbc.gridx = 0; gbc.gridy++;
        JLabel sl = new JLabel("Sleep Start (Hour 0-23):");
        sl.setForeground(TEXT);
        content.add(sl, gbc);
        gbc.gridx = 1;
        sleepStartSpinner = new JSpinner(new SpinnerNumberModel(user.getSleepStartHour(), 0, 23, 1));
        content.add(sleepStartSpinner, gbc);

        // Sleep End
        gbc.gridx = 0; gbc.gridy++;
        JLabel el = new JLabel("Sleep End (Hour 0-23):");
        el.setForeground(TEXT);
        content.add(el, gbc);
        gbc.gridx = 1;
        sleepEndSpinner = new JSpinner(new SpinnerNumberModel(user.getSleepEndHour(), 0, 23, 1));
        content.add(sleepEndSpinner, gbc);

        // Social Limit
        gbc.gridx = 0; gbc.gridy++;
        JLabel sol = new JLabel("Social Limit (min):");
        sol.setForeground(TEXT);
        content.add(sol, gbc);
        gbc.gridx = 1;
        socialLimitSpinner = new JSpinner(new SpinnerNumberModel(user.getSocialLimit(), 0, 300, 10));
        content.add(socialLimitSpinner, gbc);

        // Entertainment Limit
        gbc.gridx = 0; gbc.gridy++;
        JLabel enl = new JLabel("Entertainment Limit (min):");
        enl.setForeground(TEXT);
        content.add(enl, gbc);
        gbc.gridx = 1;
        entertainmentLimitSpinner = new JSpinner(new SpinnerNumberModel(user.getEntertainmentLimit(), 0, 300, 10));
        content.add(entertainmentLimitSpinner, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 0, 8);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false);
        
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        btns.add(cancel);

        JButton save = new JButton("Save Changes");
        save.setBackground(ACCENT);
        save.setForeground(Color.WHITE);
        save.addActionListener(e -> {
            user.setName(nameField.getText());
            user.setDailySafeLimit((int) limitSpinner.getValue());
            user.setSleepStartHour((int) sleepStartSpinner.getValue());
            user.setSleepEndHour((int) sleepEndSpinner.getValue());
            user.setSocialLimit((int) socialLimitSpinner.getValue());
            user.setEntertainmentLimit((int) entertainmentLimitSpinner.getValue());
            try {
                db.updateUser(user);
                saved = true;
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage());
            }
        });
        btns.add(save);
        content.add(btns, gbc);

        setContentPane(content);
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean wasSaved() { return saved; }
}
