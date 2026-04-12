package com.detox.gui;

import com.detox.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * Modal dialog for logging (or updating) today's screen time.
 * Has spinners for Study, Social, Entertainment, and Peak Usage Hour.
 */
public class LogTimeDialog extends JDialog {

    private static final Color BG      = new Color(0x2B2D3A);
    private static final Color ACCENT  = new Color(0x6C63FF);
    private static final Color ACCENT2 = new Color(0x3EC6C6);
    private static final Color TEXT    = new Color(0xEEEEEE);
    private static final Color SUBTEXT = new Color(0xAAAAAA);
    private static final Color FDBG    = new Color(0x1E2030);

    private final DatabaseManager  db;
    private final User             user;

    private JSpinner studySpinner;
    private JSpinner socialSpinner;
    private JSpinner entSpinner;
    private JSpinner peakHourSpinner;
    private JLabel   totalLabel;
    private JLabel   statusLabel;

    private boolean submitted = false;

    public LogTimeDialog(Frame owner, DatabaseManager db, User user, ScreenTimeRecord existing) {
        super(owner, "Log Today's Screen Time", true);
        this.db   = db;
        this.user = user;

        setBackground(BG);
        setResizable(false);

        JPanel content = buildContent(existing);
        setContentPane(content);
        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel buildContent(ScreenTimeRecord existing) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(28, 36, 24, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 4, 0);

        // Title
        JLabel title = new JLabel("📝  Screen Time Entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACCENT2);
        p.add(title, gbc);

        gbc.gridy++;
        JLabel subtitle = new JLabel("All values in minutes  •  Daily limit: " + user.getDailySafeLimit() + " min");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(SUBTEXT);
        p.add(subtitle, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(18, 0, 0, 0);
        p.add(makeSeparator(), gbc);

        // Spinner rows
        int defStudy = existing != null ? existing.getStudyTime()         : 0;
        int defSoc   = existing != null ? existing.getSocialTime()        : 0;
        int defEnt   = existing != null ? existing.getEntertainmentTime() : 0;
        int defPeak  = existing != null ? existing.getPeakUsageHour()     : 14;

        studySpinner    = makeSpinner(defStudy,   0, 960, 5);
        socialSpinner   = makeSpinner(defSoc,     0, 960, 5);
        entSpinner      = makeSpinner(defEnt,     0, 960, 5);
        peakHourSpinner = makeSpinner(defPeak,    0, 23,  1);

        // Add listeners to update total in real time
        ChangeListener cl = e -> updateTotal();
        studySpinner.addChangeListener(cl);
        socialSpinner.addChangeListener(cl);
        entSpinner.addChangeListener(cl);

        gbc.gridy++; gbc.insets = new Insets(14, 0, 4, 0);
        p.add(spinnerRow("📚  Study / Productive", studySpinner,  new Color(0x4CAF50)), gbc);

        gbc.gridy++; gbc.insets = new Insets(10, 0, 4, 0);
        p.add(spinnerRow("📱  Social Media",       socialSpinner, new Color(0xFFB03A)), gbc);

        gbc.gridy++;
        p.add(spinnerRow("🎮  Entertainment",       entSpinner,    new Color(0xFF6B6B)), gbc);

        gbc.gridy++; gbc.insets = new Insets(16, 0, 4, 0);
        p.add(spinnerRow("🕐  Peak Usage Hour (0-23)", peakHourSpinner, ACCENT), gbc);

        // Total display
        gbc.gridy++; gbc.insets = new Insets(16, 0, 0, 0);
        totalLabel = new JLabel();
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(TEXT);
        p.add(totalLabel, gbc);
        updateTotal();

        // Separator
        gbc.gridy++; gbc.insets = new Insets(14, 0, 0, 0);
        p.add(makeSeparator(), gbc);

        // Status
        gbc.gridy++; gbc.insets = new Insets(8, 0, 0, 0);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(0xFF6B6B));
        p.add(statusLabel, gbc);

        // Buttons
        gbc.gridy++; gbc.insets = new Insets(10, 0, 0, 0);
        p.add(buildButtonRow(), gbc);

        return p;
    }

    private JPanel spinnerRow(String labelText, JSpinner spinner, Color accent) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT);
        lbl.setPreferredSize(new Dimension(230, 28));
        row.add(lbl, BorderLayout.WEST);

        // Accent dot
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, (getHeight() - 8) / 2, 8, 8);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(12, 28); }
            @Override public boolean isOpaque() { return false; }
        };
        row.add(dot, BorderLayout.CENTER);
        row.add(spinner, BorderLayout.EAST);
        return row;
    }

    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setOpaque(false);

        JButton cancel = new JButton("Cancel");
        cancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancel.setForeground(SUBTEXT);
        cancel.setBackground(new Color(0x1E2030));
        cancel.setBorder(BorderFactory.createLineBorder(new Color(0x3A3D4A), 1, true));
        cancel.setFocusPainted(false);
        cancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancel.addActionListener(e -> dispose());
        row.add(cancel);

        JButton save = new JButton("Save Entry") {
            @Override protected void paintComponent(Graphics g) {
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
        save.setFont(new Font("Segoe UI", Font.BOLD, 13));
        save.setPreferredSize(new Dimension(130, 36));
        save.setContentAreaFilled(false);
        save.setBorderPainted(false);
        save.setFocusPainted(false);
        save.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        save.addActionListener(e -> doSave());
        row.add(save);

        return row;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Actions
    // ─────────────────────────────────────────────────────────────────────────

    private void doSave() {
        int study  = (int) studySpinner.getValue();
        int social = (int) socialSpinner.getValue();
        int ent    = (int) entSpinner.getValue();
        int peak   = (int) peakHourSpinner.getValue();

        if (study + social + ent == 0) {
            statusLabel.setText("Please enter at least one non-zero value.");
            return;
        }

        ScreenTimeRecord record = new ScreenTimeRecord(
            user.getId(), study, social, ent, peak
        );
        try {
            db.saveRecord(record);
            submitted = true;
            dispose();
        } catch (SQLException ex) {
            statusLabel.setText("Error saving: " + ex.getMessage());
        }
    }

    private void updateTotal() {
        int total = (int) studySpinner.getValue()
                  + (int) socialSpinner.getValue()
                  + (int) entSpinner.getValue();
        int limit = user.getDailySafeLimit();
        String status = total > limit
            ? String.format("⚠  %d min over your daily limit", total - limit)
            : "✅  Within your daily limit";
        totalLabel.setText(String.format("Total: %d min   %s", total, status));
        totalLabel.setForeground(total > limit ? new Color(0xFFB03A) : new Color(0x4CAF50));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private JSpinner makeSpinner(int val, int min, int max, int step) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(val, min, max, step));
        s.setFont(new Font("Segoe UI", Font.BOLD, 14));
        s.setPreferredSize(new Dimension(100, 32));
        s.setBackground(FDBG);
        JComponent ed = s.getEditor();
        if (ed instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(FDBG);
            de.getTextField().setForeground(TEXT);
            de.getTextField().setFont(new Font("Segoe UI", Font.BOLD, 14));
            de.getTextField().setCaretColor(ACCENT2);
            de.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        }
        return s;
    }

    private JSeparator makeSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x3A3D4A));
        return sep;
    }

    /** Returns true if the user saved a record (vs cancelling). */
    public boolean wasSubmitted() { return submitted; }

    // Required ChangeListener import alias
    private interface ChangeListener extends javax.swing.event.ChangeListener {}
}
