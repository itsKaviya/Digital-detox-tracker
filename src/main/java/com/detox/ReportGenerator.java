package com.detox;
/**
 * Generates a structured daily report to the console.
 */
public class ReportGenerator {

    private final Analyzer              analyzer              = new Analyzer();
    private final DetoxScoreCalculator  detoxScoreCalculator  = new DetoxScoreCalculator();
    private final SuggestionEngine      suggestionEngine       = new SuggestionEngine();

    /**
     * Prints a complete daily report.
     *
     * @param record   today's screen-time data
     * @param user     user profile
     * @param streak   how many consecutive low-usage days
     */
    public void printDailyReport(ScreenTimeRecord record, User user, int streak) {
        String pattern    = analyzer.analyzePattern(record, user);
        int    score      = detoxScoreCalculator.calculateScore(record, user, pattern);
        String label      = detoxScoreCalculator.scoreLabel(score);
        String suggestion = suggestionEngine.getSuggestion(record, user, pattern);

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           📊  DIGITAL DETOX TRACKER – DAILY REPORT           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.printf("  👤 User          : %s%n",      user.getName());
        System.out.printf("  📅 Date          : %s%n",      record.getDate());
        System.out.printf("  🛡  Safe Limit    : %d min%n",  user.getDailySafeLimit());
        System.out.println();
        System.out.println("  ── Screen Time Breakdown ──────────────────────────────────");
        System.out.printf("  📚 Study          : %d min%n", record.getStudyTime());
        System.out.printf("  📱 Social Media   : %d min%n", record.getSocialTime());
        System.out.printf("  🎮 Entertainment  : %d min%n", record.getEntertainmentTime());
        System.out.printf("  ⏱  Total          : %d min%n", record.getTotalTime());
        System.out.printf("  🕐 Peak Usage Hr  : %02d:00%n", record.getPeakUsageHour());
        System.out.println();
        System.out.println("  ── Behavioral Analysis ─────────────────────────────────────");
        System.out.printf("  🔍 Pattern        : %s%n", analyzer.patternDescription(pattern));
        System.out.println();
        System.out.println("  ── Detox Score ─────────────────────────────────────────────");
        System.out.printf("  🏆 Score          : %d / 100  [%s]%n", score, label);
        printScoreBar(score);
        System.out.println();
        System.out.printf("  🔥 Current Streak : %d day(s) within safe limit%n", streak);
        System.out.println();
        System.out.println("  ── Personalized Suggestion ─────────────────────────────────");
        System.out.println("  💡 " + wrapText(suggestion, 60, "     "));
        System.out.println();
        System.out.println("  ── Weekly Summary Note ──────────────────────────────────────");
        System.out.println("  📈 Use option [4] in the main menu to view your saved history.");
        System.out.println("══════════════════════════════════════════════════════════════");
    }

    /** Renders a simple ASCII progress bar for the detox score. */
    private void printScoreBar(int score) {
        int filled = score / 5; // 20 segments for 100 points
        StringBuilder bar = new StringBuilder("  [");
        for (int i = 0; i < 20; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        bar.append("]");
        System.out.println(bar);
    }

    /** Wraps long text at word boundaries for clean console output. */
    private String wrapText(String text, int lineWidth, String prefix) {
        StringBuilder result = new StringBuilder();
        String[] words = text.split(" ");
        int lineLen = 0;
        for (String word : words) {
            if (lineLen + word.length() + 1 > lineWidth && lineLen > 0) {
                result.append("\n").append(prefix);
                lineLen = 0;
            }
            if (lineLen > 0) { result.append(" "); lineLen++; }
            result.append(word);
            lineLen += word.length();
        }
        return result.toString();
    }
}
