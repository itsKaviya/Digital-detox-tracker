package com.detox.gui;

import com.detox.DatabaseManager;
import com.detox.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

/**
 * A premium login/register screen.
 * Left half: branding panel with gradient and tagline.
 * Right half: a floating card with Login and Register tabs.
 */
public class LoginPanel extends JPanel {

    // Accent colours
    private static final Color ACCENT    = new Color(0x6C63FF);
    private static final Color ACCENT2   = new Color(0x3EC6C6);
    private static final Color CARD_BG   = new Color(0x2B2D3A);
    private static final Color FIELD_BG  = new Color(0x1E2030);
    private static final Color TEXT_MAIN = new Color(0xEEEEEE);
    private static final Color TEXT_SUB  = new Color(0xAAAAAA);

    private final DatabaseManager db;
    private final MainFrame       frame;

    // Login tab fields
    private JTextField     loginUsernameField;
    private JPasswordField loginPasswordField;
    private JLabel         loginStatusLabel;

    // Register tab fields
    private JTextField     regUsernameField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmField;
    private JTextField     regNameField;
    private JSpinner       regLimitSpinner;
    private JSpinner       regSleepStartSpinner;
    private JSpinner       regSleepEndSpinner;
    private JLabel         regStatusLabel;

    private JTabbedPane tabs;

    public LoginPanel(DatabaseManager db, MainFrame frame) {
        this.db    = db;
        this.frame = frame;
        setLayout(new GridLayout(1, 2));
        add(buildBrandingPanel());
        add(buildFormPanel());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Left: Branding
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildBrandingPanel() {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x1A0533),
                        getWidth(), getHeight(), new Color(0x0D2A40));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 40, 10, 40);
        gbc.anchor = GridBagConstraints.CENTER;

        // Glowing icon label
        JLabel icon = makeGradientLabel("🌿", 80);
        p.add(icon, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(8, 40, 6, 40);
        JLabel title = new JLabel("Digital Detox");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        p.add(title, gbc);

        gbc.gridy++;
        JLabel sub = new JLabel("Tracker");
        sub.setFont(new Font("Segoe UI", Font.BOLD, 28));
        sub.setForeground(ACCENT2);
        p.add(sub, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 50, 0, 50);
        JLabel tagline = new JLabel("<html><div style='text-align:center;'>Build healthier screen habits.<br>One day at a time.</div></html>");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagline.setForeground(TEXT_SUB);
        tagline.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(tagline, gbc);

        // Decorative dots
        gbc.gridy++;
        gbc.insets = new Insets(40, 40, 0, 40);
        p.add(makeDotsPanel(), gbc);

        return p;
    }

    private JLabel makeGradientLabel(String text, int size) {
        return new JLabel(text) {
            @Override
            public Dimension getPreferredSize() { return new Dimension(size + 20, size + 20); }
        };
    }

    private JPanel makeDotsPanel() {
        JPanel dots = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color[] cols = {ACCENT, ACCENT2, new Color(0xFF6B9D)};
                for (int i = 0; i < 3; i++) {
                    g2.setColor(cols[i]);
                    g2.fillOval(i * 22, 0, 12, 12);
                }
            }
            @Override public Dimension getPreferredSize() { return new Dimension(60, 12); }
            @Override public boolean isOpaque() { return false; }
        };
        return dots;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Right: Tabbed Form Card
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(0x1E2030));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x6C63FF, true).darker(), 1, true),
            new EmptyBorder(30, 40, 30, 40)
        ));
        card.setPreferredSize(new Dimension(420, 520));

        tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.addTab("Sign In", buildLoginTab());
        tabs.addTab("Register", buildRegisterTab());
        card.add(tabs, BorderLayout.CENTER);

        wrapper.add(card);
        return wrapper;
    }

    private JPanel buildLoginTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD_BG);
        p.setBorder(new EmptyBorder(20, 0, 10, 0));
        GridBagConstraints gbc = baseGbc();

        // Username
        gbc.gridy = 0;
        p.add(label("Username"), gbc);
        gbc.gridy++;
        loginUsernameField = styledField();
        loginUsernameField.putClientProperty("JTextField.placeholderText", "Enter your username");
        p.add(loginUsernameField, gbc);

        // Password
        gbc.gridy++;
        gbc.insets.top = 14;
        p.add(label("Password"), gbc);
        gbc.gridy++;
        gbc.insets.top = 4;
        loginPasswordField = styledPasswordField();
        loginPasswordField.putClientProperty("JTextField.placeholderText", "Enter your password");
        p.add(loginPasswordField, gbc);

        // Status
        gbc.gridy++;
        gbc.insets.top = 8;
        loginStatusLabel = new JLabel(" ");
        loginStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loginStatusLabel.setForeground(new Color(0xFF6B6B));
        p.add(loginStatusLabel, gbc);

        // Login button
        gbc.gridy++;
        gbc.insets.top = 10;
        JButton btn = accentButton("Sign In →");
        btn.addActionListener(e -> doLogin());
        p.add(btn, gbc);

        // Enter key
        loginPasswordField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });

        return p;
    }

    private JPanel buildRegisterTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD_BG);
        p.setBorder(new EmptyBorder(10, 0, 10, 0));
        GridBagConstraints gbc = baseGbc();

        // Three-column row for name, username
        gbc.gridy = 0;
        p.add(label("Full Name"), gbc);
        gbc.gridy++;
        regNameField = styledField();
        regNameField.putClientProperty("JTextField.placeholderText", "Your display name");
        p.add(regNameField, gbc);

        gbc.gridy++;
        gbc.insets.top = 10;
        p.add(label("Username"), gbc);
        gbc.gridy++;
        gbc.insets.top = 4;
        regUsernameField = styledField();
        regUsernameField.putClientProperty("JTextField.placeholderText", "Unique username");
        p.add(regUsernameField, gbc);

        gbc.gridy++;
        gbc.insets.top = 10;
        p.add(label("Password"), gbc);
        gbc.gridy++;
        gbc.insets.top = 4;
        regPasswordField = styledPasswordField();
        p.add(regPasswordField, gbc);

        gbc.gridy++;
        gbc.insets.top = 10;
        p.add(label("Confirm Password"), gbc);
        gbc.gridy++;
        gbc.insets.top = 4;
        regConfirmField = styledPasswordField();
        p.add(regConfirmField, gbc);

        // Inline row: safe limit, sleep hours
        gbc.gridy++;
        gbc.insets.top = 12;

        JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
        row.setBackground(CARD_BG);

        JPanel limPanel = new JPanel(new BorderLayout(0, 4));
        limPanel.setBackground(CARD_BG);
        limPanel.add(label("Limit (min)"), BorderLayout.NORTH);
        regLimitSpinner = new JSpinner(new SpinnerNumberModel(120, 30, 480, 10));
        styleSpinner(regLimitSpinner);
        limPanel.add(regLimitSpinner, BorderLayout.CENTER);
        row.add(limPanel);

        JPanel ssPanel = new JPanel(new BorderLayout(0, 4));
        ssPanel.setBackground(CARD_BG);
        ssPanel.add(label("Sleep Start"), BorderLayout.NORTH);
        regSleepStartSpinner = new JSpinner(new SpinnerNumberModel(22, 0, 23, 1));
        styleSpinner(regSleepStartSpinner);
        ssPanel.add(regSleepStartSpinner, BorderLayout.CENTER);
        row.add(ssPanel);

        JPanel sePanel = new JPanel(new BorderLayout(0, 4));
        sePanel.setBackground(CARD_BG);
        sePanel.add(label("Sleep End"), BorderLayout.NORTH);
        regSleepEndSpinner = new JSpinner(new SpinnerNumberModel(6, 0, 12, 1));
        styleSpinner(regSleepEndSpinner);
        sePanel.add(regSleepEndSpinner, BorderLayout.CENTER);
        row.add(sePanel);

        p.add(row, gbc);

        // Status
        gbc.gridy++;
        gbc.insets.top = 8;
        regStatusLabel = new JLabel(" ");
        regStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        regStatusLabel.setForeground(new Color(0xFF6B6B));
        p.add(regStatusLabel, gbc);

        gbc.gridy++;
        gbc.insets.top = 6;
        JButton btn = accentButton("Create Account →");
        btn.addActionListener(e -> doRegister());
        p.add(btn, gbc);

        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Actions
    // ─────────────────────────────────────────────────────────────────────────

    private void doLogin() {
        String user = loginUsernameField.getText().trim();
        String pass = new String(loginPasswordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            loginStatusLabel.setText("Please fill in all fields.");
            return;
        }
        try {
            User u = db.loginUser(user, pass);
            if (u != null) {
                frame.showDashboard(u);
            } else {
                loginStatusLabel.setText("Invalid username or password.");
            }
        } catch (SQLException ex) {
            loginStatusLabel.setText("Database error: " + ex.getMessage());
        }
    }

    private void doRegister() {
        String name   = regNameField.getText().trim();
        String user   = regUsernameField.getText().trim();
        String pass   = new String(regPasswordField.getPassword());
        String confirm= new String(regConfirmField.getPassword());
        int    limit  = (int) regLimitSpinner.getValue();
        int    sStart = (int) regSleepStartSpinner.getValue();
        int    sEnd   = (int) regSleepEndSpinner.getValue();

        if (name.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            regStatusLabel.setText("Please fill in all required fields.");
            return;
        }
        if (!pass.equals(confirm)) {
            regStatusLabel.setText("Passwords do not match.");
            return;
        }
        if (pass.length() < 6) {
            regStatusLabel.setText("Password must be at least 6 characters.");
            return;
        }
        try {
            if (db.usernameExists(user)) {
                regStatusLabel.setText("Username already taken.");
                return;
            }
            User u = db.registerUser(user, pass, name, limit, sStart, sEnd);
            if (u != null) {
                frame.showDashboard(u);
            }
        } catch (SQLException ex) {
            regStatusLabel.setText("Database error: " + ex.getMessage());
        }
    }

    /** Resets both forms (called when returning from Dashboard). */
    public void reset() {
        loginUsernameField.setText("");
        loginPasswordField.setText("");
        loginStatusLabel.setText(" ");
        tabs.setSelectedIndex(0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private GridBagConstraints baseGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0;
        g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 0, 0);
        return g;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_SUB);
        return l;
    }

    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBackground(FIELD_BG);
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(ACCENT2);
        f.setPreferredSize(new Dimension(340, 38));
        return f;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBackground(FIELD_BG);
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(ACCENT2);
        f.setPreferredSize(new Dimension(340, 38));
        return f;
    }

    private void styleSpinner(JSpinner s) {
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        s.setBackground(FIELD_BG);
        s.setForeground(TEXT_MAIN);
        JComponent editor = s.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(FIELD_BG);
            de.getTextField().setForeground(TEXT_MAIN);
        }
    }

    private JButton accentButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), 0, ACCENT2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(340, 42));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
