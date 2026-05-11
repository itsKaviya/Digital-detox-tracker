package com.detox;
/**
 * Analysis engine that detects usage patterns from a ScreenTimeRecord
 * relative to the user's preferences.
 */
public class Analyzer {

    /**
     * Detects the dominant behavioral pattern and returns a short description.
     */
    public String analyzePattern(ScreenTimeRecord record, User user) {
        int total       = record.getTotalTime();
        int social      = record.getSocialTime();
        int safeLimit   = user.getDailySafeLimit();
        int peakHour    = record.getPeakUsageHour();
        int sleepStart  = user.getSleepStartHour();

        // Late-night usage: peak usage falls within sleep window
        boolean isLateNight = isLateNight(peakHour, sleepStart);

        if (isLateNight && total > safeLimit) {
            return "LATE_NIGHT_OVERUSE";
        } else if (isLateNight) {
            return "LATE_NIGHT_USAGE";
        } else if (social > safeLimit * 0.6) {
            return "HIGH_SOCIAL_MEDIA";
        } else if (total > safeLimit * 1.5) {
            return "SEVERE_OVERUSE";
        } else if (total > safeLimit) {
            return "EXCESS_TOTAL_USAGE";
        } else if (record.getStudyTime() >= total * 0.7) {
            return "PRODUCTIVE_USAGE";
        } else {
            return "BALANCED_USAGE";
        }
    }

    /** Returns true when the peakHour is inside the sleep window. */
    private boolean isLateNight(int peakHour, int sleepStart) {
        // Sleep window: sleepStart to midnight (or until morning)
        return peakHour >= sleepStart || peakHour < 6;
    }

    /** Human-readable label for a pattern code. */
    public String patternDescription(String pattern) {
        switch (pattern) {
            case "LATE_NIGHT_OVERUSE":  return "Late-night screen time combined with overuse";
            case "LATE_NIGHT_USAGE":    return "Screen use detected during sleep hours";
            case "HIGH_SOCIAL_MEDIA":   return "Excessive social media consumption";
            case "SEVERE_OVERUSE":      return "Severely exceeding your daily safe limit";
            case "EXCESS_TOTAL_USAGE":  return "Total usage exceeds the safe limit";
            case "PRODUCTIVE_USAGE":    return "Mostly study/productive screen time — great focus!";
            case "BALANCED_USAGE":      return "Balanced usage within healthy limits";
            default:                    return "Unclassified pattern";
        }
    }

    /** Simple predictive model based on last 3 days trend. */
    public int predictFutureScore(java.util.List<ScreenTimeRecord> history, User user, DetoxScoreCalculator calc) {
        if (history.size() < 3) return -1;
        
        int score1 = calc.calculateScore(history.get(0), user, analyzePattern(history.get(0), user));
        int score2 = calc.calculateScore(history.get(1), user, analyzePattern(history.get(1), user));
        int score3 = calc.calculateScore(history.get(2), user, analyzePattern(history.get(2), user));
        
        // Simple linear extrapolation
        double trend = ((score1 - score2) + (score2 - score3)) / 2.0;
        int prediction = (int) (score1 + trend);
        return Math.max(0, Math.min(100, prediction));
    }
}
