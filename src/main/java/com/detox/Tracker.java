package com.detox;
/**
 * Tracker orchestrates a full session:
 * input → analyse → alert → save → report.
 */
public class Tracker {

    private final Analyzer             analyzer    = new Analyzer();
    private final AlertSystem          alertSystem  = new AlertSystem();
    private final FileManager          fileManager  = new FileManager();
    private final ReportGenerator      reporter     = new ReportGenerator();

    /**
     * Runs the full tracking pipeline for one day's entry.
     *
     * @param user            the active user
     * @param studyTime       minutes of study screen time
     * @param socialTime      minutes of social-media screen time
     * @param entertainmentTime minutes of entertainment screen time
     * @param peakUsageHour   hour (0-23) when usage was highest
     */
    public void processDay(User user, int studyTime, int socialTime,
                           int entertainmentTime, int peakUsageHour) {

        // 1. Build the record
        ScreenTimeRecord record = new ScreenTimeRecord(
            user.getName(), studyTime, socialTime, entertainmentTime, peakUsageHour
        );

        // 2. Analyse pattern
        String pattern = analyzer.analyzePattern(record, user);

        // 3. Show alerts
        alertSystem.checkAndAlert(record, user);

        // 4. Save to file
        fileManager.saveRecord(record);

        // 5. Calculate streak
        int streak = fileManager.calculateStreak(user.getName(), user.getDailySafeLimit());

        // 6. Print full report
        reporter.printDailyReport(record, user, streak);
    }
}
