package com.detox.gui;

import com.detox.ScreenTimeRecord;
import com.detox.DetoxScoreCalculator;
import com.detox.Analyzer;
import com.detox.User;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WeeklyChartPanel extends JPanel {
    private List<ScreenTimeRecord> records;
    private final User user;
    private final Analyzer analyzer = new Analyzer();
    private final DetoxScoreCalculator calculator = new DetoxScoreCalculator();

    private static final Color LINE_COLOR = new Color(0x6C63FF);
    private static final Color DOT_COLOR  = new Color(0x3EC6C6);
    private static final Color GRID_COLOR = new Color(0x3A3D4A);
    private static final Color TEXT_COLOR = new Color(0xAAAAAA);

    public WeeklyChartPanel(User user) {
        this.user = user;
        setOpaque(false);
        setPreferredSize(new Dimension(0, 150));
    }

    public void setRecords(List<ScreenTimeRecord> records) {
        this.records = records;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (records == null || records.isEmpty()) {
            drawPlaceholder((Graphics2D) g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int pad = 30;
        int graphW = w - pad * 2;
        int graphH = h - pad * 2;

        // Draw grid
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(1));
        for (int i = 0; i <= 4; i++) {
            int y = pad + (graphH * i / 4);
            g2.drawLine(pad, y, w - pad, y);
            g2.setColor(TEXT_COLOR);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString(String.valueOf(100 - i * 25), 5, y + 4);
            g2.setColor(GRID_COLOR);
        }

        // Draw points
        int n = records.size();
        int stepX = n > 1 ? graphW / (n - 1) : 0;
        int[] xPoints = new int[n];
        int[] yPoints = new int[n];

        for (int i = 0; i < n; i++) {
            ScreenTimeRecord r = records.get(n - 1 - i); // order by date ASC for chart
            String pattern = analyzer.analyzePattern(r, user);
            int score = calculator.calculateScore(r, user, pattern);

            xPoints[i] = pad + i * stepX;
            yPoints[i] = pad + graphH - (score * graphH / 100);
        }

        // Draw line
        g2.setColor(LINE_COLOR);
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawPolyline(xPoints, yPoints, n);

        // Draw dots
        for (int i = 0; i < n; i++) {
            g2.setColor(DOT_COLOR);
            g2.fillOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1));
            g2.drawOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
        }
    }

    private void drawPlaceholder(Graphics2D g2) {
        g2.setColor(TEXT_COLOR);
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        String msg = "Not enough data for trend analysis";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }
}
