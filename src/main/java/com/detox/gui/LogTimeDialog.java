package com.detox.gui;

import com.detox.*;
import com.detox.AppTracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * Modal dialog for logging today's screen time.
 * Each category uses separate Hours and Minutes spinners.
 * An info button on each row shows why that input is collected.
 */
public class LogTimeDialog extends JDialog {

    private static final Color BG      = new Color(0x2B2D3A);
    private static final Color ACCENT  = new Color(0x6C63FF);
    private static final Color ACCENT2 = new Color(0x3EC6C6);
    private static final Color TEXT    = new Color(0xEEEEEE);
    private static final Color SUBTEXT = new Color(0xAAAAAA);
    private static final Color FDBG    = new Color(0x1E2030);
    private static final Color INFO_C  = new Color(0x6C63FF);

    private final DatabaseManager db;
    private final User user;

    // Study
    private JSpinner studyHrsSpinner;
    private JSpinner studyMinSpinner;

    // Social Media
    private JSpinner socialHrsSpinner;
    private JSpinner socialMinSpinner;

    // Entertainment
    private JSpinner entHrsSpinner;
    private JSpinner entMinSpinner;

    // Peak Usage Hour
    private JSpinner peakHourSpinner;

    // Emotion Selection
    private JComboBox<String> emotionCombo;

    private JLabel totalLabel;
    private JLabel statusLabel;
    private boolean submitted = false;

    // Info descriptions for each category
    private static final String INFO_STUDY =
        "<html><b>📚 Study / Productive Time</b><br><br>" +
        "Time spent on educational or work-related activities,<br>" +
        "e.g. online courses, coding, reading PDFs, research.<br><br>" +
        "This is counted as <b>positive screen time</b> and lowers<br>" +
        "your detox score penalty.</html>";

    private static final String INFO_SOCIAL =
        "<html><b>📱 Social Media Time</b><br><br>" +
        "Time spent scrolling or interacting on platforms like<br>" +
        "Instagram, Twitter/X, WhatsApp, Reddit, etc.<br><br>" +
        "High social media use raises your <b>detox alert level</b><br>" +
        "and impacts your wellness score negatively.</html>";

    private static final String INFO_ENT =
        "<html><b>🎮 Entertainment Time</b><br><br>" +
        "Time spent on gaming, streaming videos (YouTube,<br>" +
        "Netflix), or other leisure screen activities.<br><br>" +
        "Balanced entertainment is fine — we track it to give<br>" +
        "you an honest total picture of your day.</html>";

    private static final String INFO_PEAK =
        "<html><b>🕐 Peak Usage Hour</b><br><br>" +
        "The hour (0–23) when you used your devices the most.<br>" +
        "E.g. if you were most active at 9 PM, enter 21.<br><br>" +
        "This helps detect <b>late-night usage patterns</b> that<br>" +
        "can disrupt sleep and affect your detox score.</html>";

    // ─────────────────────────────────────────────────────────────────────────

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

        // ── Title ──────────────────────────────────────────────────────────
        JLabel title = new JLabel("  Screen Time Entry");
        title.setIcon(UIUtils.getIcon("pencil", 20, ACCENT2));
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACCENT2);
        p.add(title, gbc);

        gbc.gridy++;
        JLabel subtitle = new JLabel("Enter hours and/or minutes for each category  •  Daily limit: "
                + formatTime(user.getDailySafeLimit()));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(SUBTEXT);
        p.add(subtitle, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(18, 0, 0, 0);
        p.add(makeSeparator(), gbc);

        // ── Column header row ──────────────────────────────────────────────
        gbc.gridy++;
        gbc.insets = new Insets(14, 0, 2, 0);
        p.add(buildColumnHeaders(), gbc);

        // ── Existing defaults ──────────────────────────────────────────────
        AppTracker tracker = AppTracker.getInstance();
        int defStudyMin = existing != null ? existing.getStudyTime()         : tracker.getStudyMinutes();
        int defSocMin   = existing != null ? existing.getSocialTime()        : tracker.getSocialMinutes();
        int defEntMin   = existing != null ? existing.getEntertainmentTime() : tracker.getEntertainmentMinutes();
        int defPeak     = existing != null ? existing.getPeakUsageHour()     : 14;

        if (existing == null && (defStudyMin > 0 || defSocMin > 0 || defEntMin > 0)) {
            subtitle.setText("<html>" + subtitle.getText() + "<br><font color='#6C63FF'><b>✨ Pre-filled with automatically tracked app usage</b></font></html>");
        }

        // ── Study row ──────────────────────────────────────────────────────
        studyHrsSpinner = makeHrSpinner(defStudyMin / 60);
        studyMinSpinner = makeMinSpinner(defStudyMin % 60);
        gbc.gridy++; gbc.insets = new Insets(8, 0, 4, 0);
        p.add(categoryRow("Study / Productive", "book", new Color(0x4CAF50),
                studyHrsSpinner, studyMinSpinner, INFO_STUDY), gbc);

        // ── Social row ─────────────────────────────────────────────────────
        socialHrsSpinner = makeHrSpinner(defSocMin / 60);
        socialMinSpinner = makeMinSpinner(defSocMin % 60);
        gbc.gridy++; gbc.insets = new Insets(6, 0, 4, 0);
        p.add(categoryRow("Social Media", "phone", new Color(0xFFB03A),
                socialHrsSpinner, socialMinSpinner, INFO_SOCIAL), gbc);

        // ── Entertainment row ──────────────────────────────────────────────
        entHrsSpinner = makeHrSpinner(defEntMin / 60);
        entMinSpinner = makeMinSpinner(defEntMin % 60);
        gbc.gridy++;
        p.add(categoryRow("Entertainment", "game", new Color(0xFF6B6B),
                entHrsSpinner, entMinSpinner, INFO_ENT), gbc);

        // ── Separator ─────────────────────────────────────────────────────
        gbc.gridy++; gbc.insets = new Insets(14, 0, 0, 0);
        p.add(makeSeparator(), gbc);

        // ── Peak Hour row ──────────────────────────────────────────────────
        peakHourSpinner = makeHrSpinner(defPeak);
        ((SpinnerNumberModel) peakHourSpinner.getModel()).setMaximum(23);
        gbc.gridy++; gbc.insets = new Insets(12, 0, 4, 0);
        p.add(peakHourRow(), gbc);

        // ── Emotion row ──
        gbc.gridy++; gbc.insets = new Insets(6, 0, 4, 0);
        p.add(emotionRow(), gbc);

        // ── Total display ──────────────────────────────────────────────────
        gbc.gridy++; gbc.insets = new Insets(16, 0, 0, 0);
        totalLabel = new JLabel();
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(TEXT);
        p.add(totalLabel, gbc);
        attachListeners();
        updateTotal();

        // ── Bottom separator ───────────────────────────────────────────────
        gbc.gridy++; gbc.insets = new Insets(14, 0, 0, 0);
        p.add(makeSeparator(), gbc);

        // ── Status ─────────────────────────────────────────────────────────
        gbc.gridy++; gbc.insets = new Insets(8, 0, 0, 0);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(0xFF6B6B));
        p.add(statusLabel, gbc);

        // ── Buttons ────────────────────────────────────────────────────────
        gbc.gridy++; gbc.insets = new Insets(10, 0, 0, 0);
        p.add(buildButtonRow(), gbc);

        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Row Builders
    // ─────────────────────────────────────────────────────────────────────────

    /** Builds the "Hours  |  Minutes" column header strip. */
    private JPanel buildColumnHeaders() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(220, 18));
        p.add(spacer, BorderLayout.WEST);

        JPanel headers = new JPanel(new GridLayout(1, 2, 4, 0));
        headers.setOpaque(false);

        JLabel hrsHdr = new JLabel("Hours", SwingConstants.CENTER);
        hrsHdr.setFont(new Font("Segoe UI", Font.BOLD, 11));
        hrsHdr.setForeground(SUBTEXT);

        JLabel minHdr = new JLabel("Mins", SwingConstants.CENTER);
        minHdr.setFont(new Font("Segoe UI", Font.BOLD, 11));
        minHdr.setForeground(SUBTEXT);

        headers.add(hrsHdr);
        headers.add(minHdr);

        // Reserve space for info button
        JLabel infoSpacer = new JLabel("");
        infoSpacer.setPreferredSize(new Dimension(30, 18));

        JPanel right = new JPanel(new BorderLayout(6, 0));
        right.setOpaque(false);
        right.add(headers, BorderLayout.CENTER);
        right.add(infoSpacer, BorderLayout.EAST);

        p.add(right, BorderLayout.CENTER);
        return p;
    }

    /**
     * Builds one category row with:
     * [emoji label] [accent dot] [hrs spinner] [mins spinner] [ℹ button]
     */
    private JPanel categoryRow(String label, String iconName, Color accent,
                               JSpinner hrsSpinner, JSpinner minSpinner,
                               String infoHtml) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        // Label
        JLabel lbl = new JLabel("  " + label);
        lbl.setIcon(UIUtils.getIcon(iconName, 16, TEXT));

        // Accent dot
        JPanel dot = makeDot(accent);

        JPanel left = new JPanel(new BorderLayout(6, 0));
        left.setOpaque(false);
        left.add(lbl, BorderLayout.WEST);
        left.add(dot, BorderLayout.CENTER);
        row.add(left, BorderLayout.WEST);

        // Spinners side by side
        JPanel spinners = new JPanel(new GridLayout(1, 2, 4, 0));
        spinners.setOpaque(false);
        spinners.add(hrsSpinner);
        spinners.add(minSpinner);

        // Info button
        JButton info = makeInfoButton(infoHtml);

        JPanel right = new JPanel(new BorderLayout(8, 0));
        right.setOpaque(false);
        right.add(spinners, BorderLayout.CENTER);
        right.add(info, BorderLayout.EAST);

        row.add(right, BorderLayout.CENTER);
        return row;
    }

    /** Peak hour row (single spinner + info button). */
    private JPanel peakHourRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel("  Peak Usage Hour (0 – 23)");
        lbl.setIcon(UIUtils.getIcon("clock", 16, TEXT));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT);
        lbl.setPreferredSize(new Dimension(210, 30));

        JPanel dot = makeDot(ACCENT);

        JPanel left = new JPanel(new BorderLayout(6, 0));
        left.setOpaque(false);
        left.add(lbl, BorderLayout.WEST);
        left.add(dot, BorderLayout.CENTER);
        row.add(left, BorderLayout.WEST);

        // Single spinner + matching-width blank panel on the right
        JPanel spinners = new JPanel(new GridLayout(1, 2, 4, 0));
        spinners.setOpaque(false);
        spinners.add(peakHourSpinner);
        JLabel gap = new JLabel(); // visual balance
        gap.setOpaque(false);
        spinners.add(gap);

        JButton info = makeInfoButton(INFO_PEAK);

        JPanel right = new JPanel(new BorderLayout(8, 0));
        right.setOpaque(false);
        right.add(spinners, BorderLayout.CENTER);
        right.add(info, BorderLayout.EAST);

        row.add(right, BorderLayout.CENTER);
        return row;
    }

    private JPanel emotionRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel("  How do you feel today?");
        lbl.setIcon(UIUtils.getIcon("leaf", 16, TEXT));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT);
        lbl.setPreferredSize(new Dimension(210, 30));

        JPanel left = new JPanel(new BorderLayout(6, 0));
        left.setOpaque(false);
        left.add(lbl, BorderLayout.WEST);
        left.add(makeDot(ACCENT2), BorderLayout.CENTER);
        row.add(left, BorderLayout.WEST);

        String[] emotions = {"Happy / Positive", "Stressed / Anxious", "Tired / Low Energy", "Productive / Focused", "Bored", "Neutral"};
        emotionCombo = new JComboBox<>(emotions);
        emotionCombo.setSelectedIndex(5); // Neutral
        emotionCombo.setBackground(FDBG);
        emotionCombo.setForeground(TEXT);
        emotionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(emotionCombo, BorderLayout.CENTER);
        // spacer to match width
        JLabel gap = new JLabel();
        gap.setPreferredSize(new Dimension(38, 30));
        right.add(gap, BorderLayout.EAST);

        row.add(right, BorderLayout.CENTER);
        return row;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Actions
    // ─────────────────────────────────────────────────────────────────────────

    private void doSave() {
        int study  = toMinutes(studyHrsSpinner,  studyMinSpinner);
        int social = toMinutes(socialHrsSpinner, socialMinSpinner);
        int ent    = toMinutes(entHrsSpinner,    entMinSpinner);
        int peak   = (int) peakHourSpinner.getValue();
        String emotion = (String) emotionCombo.getSelectedItem();

        if (study + social + ent == 0) {
            statusLabel.setText("Please enter at least one non-zero value.");
            return;
        }

        ScreenTimeRecord record = new ScreenTimeRecord(
            user.getId(), study, social, ent, peak, emotion, "", 0, 0
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
        int total = toMinutes(studyHrsSpinner,  studyMinSpinner)
                  + toMinutes(socialHrsSpinner, socialMinSpinner)
                  + toMinutes(entHrsSpinner,    entMinSpinner);
        int limit = user.getDailySafeLimit();
        String status = total > limit
            ? String.format("⚠  %s over your daily limit", formatTime(total - limit))
            : "✅  Within your daily limit";
        totalLabel.setText(String.format("Total: %s   %s", formatTime(total), status));
        totalLabel.setForeground(total > limit ? new Color(0xFFB03A) : new Color(0x4CAF50));
    }

    private void attachListeners() {
        javax.swing.event.ChangeListener cl = e -> updateTotal();
        studyHrsSpinner.addChangeListener(cl);
        studyMinSpinner.addChangeListener(cl);
        socialHrsSpinner.addChangeListener(cl);
        socialMinSpinner.addChangeListener(cl);
        entHrsSpinner.addChangeListener(cl);
        entMinSpinner.addChangeListener(cl);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private int toMinutes(JSpinner hrsSpinner, JSpinner minSpinner) {
        return (int) hrsSpinner.getValue() * 60 + (int) minSpinner.getValue();
    }

    private String formatTime(int totalMinutes) {
        int h = totalMinutes / 60;
        int m = totalMinutes % 60;
        if (h > 0 && m > 0) return h + "h " + m + "m";
        if (h > 0)           return h + "h";
        return m + "m";
    }

    private JSpinner makeHrSpinner(int val) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(val, 0, 16, 1));
        styleSpinner(s);
        return s;
    }

    private JSpinner makeMinSpinner(int val) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(val, 0, 59, 5));
        styleSpinner(s);
        return s;
    }

    private void styleSpinner(JSpinner s) {
        s.setFont(new Font("Segoe UI", Font.BOLD, 14));
        s.setPreferredSize(new Dimension(80, 32));
        s.setBackground(FDBG);
        JComponent ed = s.getEditor();
        if (ed instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(FDBG);
            de.getTextField().setForeground(TEXT);
            de.getTextField().setFont(new Font("Segoe UI", Font.BOLD, 14));
            de.getTextField().setCaretColor(ACCENT2);
            de.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    private JButton makeInfoButton(String html) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                boolean hover = getModel().isRollover();
                
                // Background circle
                g2.setColor(hover ? INFO_C : new Color(0x3A3D55));
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                
                // Border
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(hover ? Color.WHITE : INFO_C);
                g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);
                
                // Draw "i"
                g2.setColor(hover ? Color.WHITE : TEXT);
                g2.setFont(new Font("Serif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String t = "i";
                int tx = (getWidth() - fm.stringWidth(t)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1;
                g2.drawString(t, tx, ty);
                
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setMaximumSize(new Dimension(28, 28));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Click for more info");
        btn.addActionListener(e -> {
            JOptionPane pane = new JOptionPane(
                new JLabel(html),
                JOptionPane.INFORMATION_MESSAGE
            );
            JDialog dlg = pane.createDialog(this, "Field Description");
            dlg.setVisible(true);
        });
        return btn;
    }

    private JPanel makeDot(Color accent) {
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, (getHeight() - 8) / 2, 8, 8);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(14, 30); }
            @Override public boolean isOpaque() { return false; }
        };
        return dot;
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
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
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

    private JSeparator makeSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x3A3D4A));
        return sep;
    }

    /** Returns true if the user saved a record (vs cancelling). */
    public boolean wasSubmitted() { return submitted; }
}
