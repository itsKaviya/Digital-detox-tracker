package com.detox.gui;

import com.detox.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main dashboard shown after login.
 * Shows: top header, today's stats card, detox score ring, suggestion, and history table.
 */
public class DashboardPanel extends JPanel {

    // Palette
    private static final Color BG       = new Color(0x1E2030);
    private static final Color CARD_BG  = new Color(0x2B2D3A);
    private static final Color ACCENT   = new Color(0x6C63FF);
    private static final Color ACCENT2  = new Color(0x3EC6C6);
    private static final Color GOOD     = new Color(0x4CAF50);
    private static final Color WARN     = new Color(0xFFB03A);
    private static final Color BAD      = new Color(0xFF6B6B);
    private static final Color TEXT     = new Color(0xEEEEEE);
    private static final Color SUBTEXT  = new Color(0xAAAAAA);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final DatabaseManager db;
    private final User            user;
    private final MainFrame       frame;

    // Live data
    private ScreenTimeRecord todayRecord;
    private int              detoxScore;
    private String           pattern;
    private String           suggestion;

    // Dynamic components
    private JLabel  scoreValueLabel;
    private JLabel  scoreLabelLabel;
    private JLabel  patternLabel;
    private JLabel  suggestionLabel;
    private JLabel  studyBar;
    private JLabel  socialBar;
    private JLabel  entBar;
    private JLabel  totalLabel;
    private JPanel  scoreRingPanel;
    private JTable  historyTable;
    private DefaultTableModel historyModel;
    private JButton logBtn;

    private final Analyzer             analyzer  = new Analyzer();
    private final DetoxScoreCalculator calculator = new DetoxScoreCalculator();
    private final SuggestionEngine     engine     = new SuggestionEngine();

    public DashboardPanel(DatabaseManager db, User user, MainFrame frame) {
        this.db    = db;
        this.user  = user;
        this.frame = frame;
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);

        refreshData();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Header bar
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x1A0533),
                        getWidth(), 0, new Color(0x0D2A40));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        h.setPreferredSize(new Dimension(0, 64));
        h.setBorder(new EmptyBorder(0, 24, 0, 24));

        JLabel title = new JLabel("🌿 Digital Detox Tracker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        h.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel welcome = new JLabel("Hello, " + user.getName() + " 👋");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcome.setForeground(SUBTEXT);
        right.add(welcome);

        JButton logoutBtn = flatButton("Logout", BAD);
        logoutBtn.addActionListener(e -> frame.showLogin());
        right.add(logoutBtn);

        h.add(right, BorderLayout.EAST);
        return h;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Body (two-column layout)
    // ─────────────────────────────────────────────────────────────────────────

    private JScrollPane buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill   = GridBagConstraints.BOTH;

        // ── Left column (col 0): Stats card + Suggestion card ──
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.55; gbc.weighty = 0.0;
        body.add(buildStatsCard(), gbc);

        gbc.gridy = 1; gbc.weighty = 0.0;
        body.add(buildSuggestionCard(), gbc);

        gbc.gridy = 2; gbc.weighty = 1.0;
        body.add(buildHistoryCard(), gbc);

        // ── Right column (col 1): Score ring + Log button ──
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 0.45; gbc.weighty = 0.0;
        gbc.gridheight = 2;
        body.add(buildScoreCard(), gbc);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Cards
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildStatsCard() {
        JPanel card = card("📊  Today's Screen Time");

        // Row: label + bar + value for each category
        String[] cats = {"Study", "Social", "Entertainment"};
        Color[]  cols = {GOOD, WARN, BAD};

        JPanel barsPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        barsPanel.setOpaque(false);
        barsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        studyBar = barRow(cats[0], cols[0], barsPanel);
        socialBar = barRow(cats[1], cols[1], barsPanel);
        entBar    = barRow(cats[2], cols[2], barsPanel);

        card.add(barsPanel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        totalLabel = new JLabel("Total: — min  |  Limit: " + user.getDailySafeLimit() + " min");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalLabel.setForeground(SUBTEXT);
        footer.add(totalLabel, BorderLayout.WEST);

        logBtn = gradientButton("＋  Log Today's Time");
        logBtn.addActionListener(e -> openLogDialog());
        footer.add(logBtn, BorderLayout.EAST);

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    /** Creates a single bar row and adds it to the panel. Returns the value label. */
    private JLabel barRow(String name, Color barColor, JPanel parent) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameLabel.setForeground(SUBTEXT);
        nameLabel.setPreferredSize(new Dimension(110, 20));
        row.add(nameLabel, BorderLayout.WEST);

        JPanel barBg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x40404A));
                g2.fillRoundRect(0, 4, getWidth(), getHeight() - 8, 8, 8);
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        barBg.setLayout(new BorderLayout());

        JPanel barFill = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, barColor.darker(), getWidth(), 0, barColor.brighter());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 4, getWidth(), getHeight() - 8, 8, 8);
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        barFill.setName(name + "_fill");
        barFill.setPreferredSize(new Dimension(0, 28));
        barBg.add(barFill, BorderLayout.WEST);
        row.add(barBg, BorderLayout.CENTER);

        JLabel valLabel = new JLabel("— min");
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valLabel.setForeground(TEXT);
        valLabel.setPreferredSize(new Dimension(72, 20));
        valLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(valLabel, BorderLayout.EAST);

        parent.add(row);
        return valLabel;
    }

    private JPanel buildScoreCard() {
        JPanel card = card("🏅  Detox Score");
        card.setPreferredSize(new Dimension(300, 340));

        scoreRingPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawScoreRing((Graphics2D) g, getWidth(), getHeight(), detoxScore);
            }
            @Override public boolean isOpaque() { return false; }
            @Override public Dimension getPreferredSize() { return new Dimension(200, 200); }
        };

        scoreValueLabel = new JLabel("—", SwingConstants.CENTER);
        scoreValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 44));
        scoreValueLabel.setForeground(TEXT);

        scoreLabelLabel = new JLabel("Not logged yet", SwingConstants.CENTER);
        scoreLabelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoreLabelLabel.setForeground(SUBTEXT);

        patternLabel = new JLabel(" ", SwingConstants.CENTER);
        patternLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        patternLabel.setForeground(SUBTEXT);

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.insets = new Insets(4, 0, 4, 0);
        inner.add(scoreRingPanel, g);
        g.gridy++;
        inner.add(scoreValueLabel, g);
        g.gridy++;
        inner.add(scoreLabelLabel, g);
        g.gridy++;
        inner.add(patternLabel, g);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSuggestionCard() {
        JPanel card = card("💡  Wellness Tip");

        suggestionLabel = new JLabel("<html><body style='width:100%;'>" +
            "<i>Log your screen time today to get personalised suggestions.</i></body></html>");
        suggestionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        suggestionLabel.setForeground(TEXT);
        suggestionLabel.setBorder(new EmptyBorder(8, 0, 8, 0));

        card.add(suggestionLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildHistoryCard() {
        JPanel card = card("📅  Recent History (Last 7 Days)");

        String[] cols = {"Date", "Study (min)", "Social (min)", "Entertainment (min)", "Total (min)", "Score"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(30);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setForeground(TEXT);
        historyTable.setBackground(CARD_BG);
        historyTable.setGridColor(new Color(0x3A3D4A));
        historyTable.setSelectionBackground(ACCENT.darker());
        historyTable.setSelectionForeground(Color.WHITE);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(new Color(0x22243A));
        historyTable.getTableHeader().setForeground(ACCENT2);
        historyTable.getTableHeader().setBorder(new EmptyBorder(0, 0, 0, 0));
        historyTable.setShowVerticalLines(false);

        // Alternating row renderer
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel,
                                                           boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                l.setBorder(new EmptyBorder(0, 8, 0, 8));
                if (!sel) {
                    l.setBackground(row % 2 == 0 ? CARD_BG : new Color(0x252738));
                    l.setForeground(TEXT);
                }
                // Colour the score column
                if (col == 5 && val != null && !val.toString().equals("—")) {
                    try {
                        int s = Integer.parseInt(val.toString().split(" ")[0]);
                        l.setForeground(s >= 70 ? GOOD : s >= 40 ? WARN : BAD);
                    } catch (NumberFormatException ignored) {}
                }
                return l;
            }
        });

        JScrollPane sp = new JScrollPane(historyTable);
        sp.setBorder(null);
        sp.getViewport().setBackground(CARD_BG);
        sp.setPreferredSize(new Dimension(0, 180));

        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Score Ring Painter
    // ─────────────────────────────────────────────────────────────────────────

    private void drawScoreRing(Graphics2D g2, int w, int h, int score) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int pad  = 20;
        int size = Math.min(w, h) - pad * 2;
        int x    = (w - size) / 2;
        int y    = (h - size) / 2;
        int thick = 14;

        // Background arc
        g2.setStroke(new BasicStroke(thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(0x3A3D4A));
        g2.drawArc(x, y, size, size, 0, 360);

        if (score > 0) {
            // Colour arc
            Color ringColor = score >= 70 ? GOOD : score >= 40 ? WARN : BAD;
            int   angle     = (int) (score / 100.0 * 360);
            g2.setColor(ringColor);
            g2.drawArc(x, y, size, size, 90, -angle);
        }
        g2.setStroke(new BasicStroke(1));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Log Time Dialog
    // ─────────────────────────────────────────────────────────────────────────

    private void openLogDialog() {
        LogTimeDialog dialog = new LogTimeDialog(frame, db, user, todayRecord);
        dialog.setVisible(true);
        if (dialog.wasSubmitted()) {
            refreshData();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Data Refresh
    // ─────────────────────────────────────────────────────────────────────────

    public void refreshData() {
        try {
            todayRecord = db.getTodayRecord(user.getId());

            if (todayRecord != null) {
                pattern    = analyzer.analyzePattern(todayRecord, user);
                detoxScore = calculator.calculateScore(todayRecord, user, pattern);
                suggestion = engine.getSuggestion(todayRecord, user, pattern);
                updateStatsUI();
            } else {
                detoxScore = 0;
                clearStatsUI();
            }

            // History
            List<ScreenTimeRecord> history = db.getRecentRecords(user.getId(), 7);
            historyModel.setRowCount(0);
            for (ScreenTimeRecord r : history) {
                String p = analyzer.analyzePattern(r, user);
                int    s = calculator.calculateScore(r, user, p);
                historyModel.addRow(new Object[]{
                    r.getDate().format(DATE_FMT),
                    r.getStudyTime(),
                    r.getSocialTime(),
                    r.getEntertainmentTime(),
                    r.getTotalTime(),
                    s + " / 100"
                });
            }

            scoreRingPanel.repaint();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatsUI() {
        int study  = todayRecord.getStudyTime();
        int social = todayRecord.getSocialTime();
        int ent    = todayRecord.getEntertainmentTime();
        int total  = todayRecord.getTotalTime();
        int limit  = user.getDailySafeLimit();

        // Bar widths (relative to 300px max bar area)
        int maxW = 300;
        updateBarWidth(studyBar,  study,  total, maxW, "Study");
        updateBarWidth(socialBar, social, total, maxW, "Social");
        updateBarWidth(entBar,    ent,    total, maxW, "Entertainment");

        studyBar.setText(study  + " min");
        socialBar.setText(social + " min");
        entBar.setText(ent    + " min");

        totalLabel.setText(String.format("Total: %d min  |  Limit: %d min  |  %s",
            total, limit, total > limit ? "⚠ Over limit" : "✅ Within limit"));
        totalLabel.setForeground(total > limit ? WARN : GOOD);

        scoreValueLabel.setText(String.valueOf(detoxScore));
        scoreValueLabel.setForeground(detoxScore >= 70 ? GOOD : detoxScore >= 40 ? WARN : BAD);
        scoreLabelLabel.setText(calculator.scoreLabel(detoxScore));
        scoreLabelLabel.setForeground(TEXT);
        patternLabel.setText(analyzer.patternDescription(pattern));

        suggestionLabel.setText("<html><body style='width:100%;'>" +
            suggestion.replace("\n", "<br>") + "</body></html>");

        logBtn.setText("✎  Update Today's Log");
    }

    private void updateBarWidth(JLabel label, int value, int total, int maxW, String name) {
        if (total == 0) return;
        int fillW = (int) ((value / (double) total) * maxW);
        // The label's parent chain: label → row → barBg → barFill
        Container row = label.getParent();
        if (row == null) return;
        for (Component comp : row.getComponents()) {
            if (comp instanceof JPanel barBg) {
                for (Component inner : barBg.getComponents()) {
                    if (inner instanceof JPanel fill && (name + "_fill").equals(fill.getName())) {
                        fill.setPreferredSize(new Dimension(fillW, 28));
                        barBg.revalidate();
                    }
                }
            }
        }
    }

    private void clearStatsUI() {
        studyBar.setText("— min");
        socialBar.setText("— min");
        entBar.setText("— min");
        totalLabel.setText("Total: — min  |  Limit: " + user.getDailySafeLimit() + " min");
        totalLabel.setForeground(SUBTEXT);
        scoreValueLabel.setText("—");
        scoreValueLabel.setForeground(SUBTEXT);
        scoreLabelLabel.setText("Not logged yet");
        patternLabel.setText(" ");
        suggestionLabel.setText("<html><body style='width:100%;'>" +
            "<i>Log your screen time today to get personalised suggestions.</i></body></html>");
        logBtn.setText("＋  Log Today's Time");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Creates a styled card panel with a title and BorderLayout. */
    private JPanel card(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3A3D4A), 1, true),
            new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(ACCENT2);
        titleLabel.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(titleLabel, BorderLayout.NORTH);
        return card;
    }

    private JButton gradientButton(String text) {
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(200, 36));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton flatButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(color);
        btn.setBackground(new Color(0x1E2030));
        btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
