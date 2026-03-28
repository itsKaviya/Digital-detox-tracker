package com.detox;
/**
 * Calculates a Digital Detox Score (0–100) that quantifies digital health.
 *
 * Scoring model:
 *  – Start with 100 points.
 *  – Deduct proportionally for time that exceeds the safe limit.
 *  – Additional penalty for high social media share.
 *  – Additional penalty for late-night usage.
 *  – Bonus points for productive (study) usage.
 *  – Score is clamped to [0, 100].
 */
public class DetoxScoreCalculator {

    /**
     * @param record   today's screen-time data
     * @param user     user profile (safe limit, sleep window)
     * @param pattern  pattern code from Analyzer
     * @return         detox score 0-100
     */
    public int calculateScore(ScreenTimeRecord record, User user, String pattern) {
        int score = 100;
        int total     = record.getTotalTime();
        int safeLimit = user.getDailySafeLimit();

        // --- Primary penalty: excess screen time ---
        if (total > safeLimit) {
            int excess = total - safeLimit;
            // Each extra minute beyond safe limit costs ~0.5 points
            int penalty = (int)((excess / (double) safeLimit) * 50);
            score -= penalty;
        }

        // --- Secondary penalty: heavy social media use ---
        double socialRatio = (double) record.getSocialTime() / Math.max(1, total);
        if (socialRatio > 0.5) {
            score -= 15; // high social media dominance
        } else if (socialRatio > 0.35) {
            score -= 8;
        }

        // --- Tertiary penalty: late night usage ---
        if (pattern.contains("LATE_NIGHT")) {
            score -= 10;
        }

        // --- Bonus: productive usage ---
        double studyRatio = (double) record.getStudyTime() / Math.max(1, total);
        if (studyRatio >= 0.7) {
            score += 10;
        } else if (studyRatio >= 0.5) {
            score += 5;
        }

        // Clamp
        return Math.max(0, Math.min(100, score));
    }

    /** Returns a qualitative label for the score. */
    public String scoreLabel(int score) {
        if (score >= 85) return "Excellent 🌟";
        if (score >= 70) return "Good ✅";
        if (score >= 50) return "Fair ⚠️";
        if (score >= 30) return "Poor 🔴";
        return "Critical ❌";
    }
}
