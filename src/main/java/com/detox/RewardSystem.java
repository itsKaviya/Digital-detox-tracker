package com.detox;

import java.util.ArrayList;
import java.util.List;

/**
 * Gamification Module: Calculates streaks and awards badges based on digital wellness.
 */
public class RewardSystem {

    public static class Achievement {
        public final String title;
        public final String description;
        public final String icon;

        public Achievement(String title, String description, String icon) {
            this.title = title;
            this.description = description;
            this.icon = icon;
        }
    }

    public List<Achievement> getAchievements(List<ScreenTimeRecord> history, User user) {
        List<Achievement> list = new ArrayList<>();
        
        if (history.isEmpty()) return list;

        // 1. Streak Achievement
        int streak = calculateStreak(history, user);
        if (streak >= 7) {
            list.add(new Achievement("Weekly Warrior", "7-day streak within safe limits!", "medal"));
        } else if (streak >= 3) {
            list.add(new Achievement("Consistent", "3-day wellness streak!", "leaf"));
        }

        // 2. Productivity Achievement
        boolean productiveDay = history.get(0).getStudyTime() > history.get(0).getSocialTime() * 2;
        if (productiveDay) {
            list.add(new Achievement("Scholar", "Deep focus session recorded today.", "book"));
        }

        // 3. Early Bird
        if (history.get(0).getPeakUsageHour() < 12 && history.get(0).getPeakUsageHour() > 6) {
            list.add(new Achievement("Early Riser", "Most active during daylight hours.", "bulb"));
        }

        return list;
    }

    public int calculateStreak(List<ScreenTimeRecord> history, User user) {
        int streak = 0;
        int limit = user.getDailySafeLimit();
        for (ScreenTimeRecord r : history) {
            if (r.getTotalTime() <= limit) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}
