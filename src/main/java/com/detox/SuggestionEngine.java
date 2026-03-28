package com.detox;
/**
 * Generates personalized, behavior-aware suggestions based on the
 * detected usage pattern, time of use, and overuse conditions.
 */
public class SuggestionEngine {

    public String getSuggestion(ScreenTimeRecord record, User user, String pattern) {
        int total     = record.getTotalTime();
        int safeLimit = user.getDailySafeLimit();
        int social    = record.getSocialTime();
        int peakHour  = record.getPeakUsageHour();

        switch (pattern) {
            case "LATE_NIGHT_OVERUSE":
                return "You're using screens late at night AND exceeding your limit. "
                     + "Try the 20-20-20 rule and set a hard device curfew 1 hour before sleep. "
                     + "Consider using blue-light filters and a wind-down alarm.";

            case "LATE_NIGHT_USAGE":
                return "Screen use during sleep hours disrupts melatonin production. "
                     + "Set a phone-free bedroom policy and try reading a physical book instead. "
                     + "Your sleep quality will improve dramatically!";

            case "HIGH_SOCIAL_MEDIA":
                return "Social media is consuming " + social + " min of your day (" 
                     + percent(social, total) + "% of total). "
                     + "Try replacing 30 min of scrolling with an outdoor walk, creative hobby, "
                     + "or a quick call with a friend in person. Use app timers as guardrails.";

            case "SEVERE_OVERUSE":
                return "You've exceeded your safe limit by "
                     + (total - safeLimit) + " minutes — that's "
                     + percent(total - safeLimit, safeLimit) + "% over budget! "
                     + "Schedule a 'digital detox hour' tomorrow afternoon. "
                     + "Engage in physical exercise or mindfulness meditation to reset.";

            case "EXCESS_TOTAL_USAGE":
                return "You're " + (total - safeLimit) + " minutes over your safe limit today. "
                     + "Tomorrow, try the Pomodoro Technique: 25 min on, 5 min off screens. "
                     + "Break up screen time with short stretching or breathing exercises.";

            case "PRODUCTIVE_USAGE":
                return "Great work — most of your screen time was spent learning! "
                     + "Remember to protect your eyes: follow the 20-20-20 rule "
                     + "(every 20 min, look 20 ft away for 20 sec). "
                     + "Keep up the productive momentum!";

            case "BALANCED_USAGE":
                return "You're within your healthy screen time limit today. "
                     + "Keep this up — consistency is the key to digital wellness. "
                     + "Reward yourself with a tech-free activity you enjoy!";

            default:
                return "Monitor your usage patterns over the next few days for tailored advice. "
                     + "Aim to stay within your " + safeLimit + "-minute daily safe limit.";
        }
    }

    private int percent(int part, int total) {
        if (total == 0) return 0;
        return (int) Math.round((part * 100.0) / total);
    }
}
