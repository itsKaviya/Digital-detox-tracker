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
        String emotion = record.getEmotion() != null ? record.getEmotion().toLowerCase() : "neutral";

        String baseAdvice;
        String alternateActivity;

        // Base pattern-based advice
        switch (pattern) {
            case "LATE_NIGHT_OVERUSE":
                baseAdvice = "Late-night overuse detected. Device curfew 1 hour before sleep is highly recommended.";
                alternateActivity = "Read a physical book / Guided sleep meditation";
                break;
            case "LATE_NIGHT_USAGE":
                baseAdvice = "Screen use during sleep hours disrupts rest. Bedroom should be a tech-free zone.";
                alternateActivity = "Journaling / Herbal tea / Light stretching";
                break;
            case "HIGH_SOCIAL_MEDIA":
                baseAdvice = "Social media is dominating your screen time. Mindful scrolling is key.";
                alternateActivity = "Sketching / 15-min walk / Call a family member";
                break;
            case "SEVERE_OVERUSE":
                baseAdvice = "Severe screen overuse today. Your brain needs a complete digital reset.";
                alternateActivity = "Outdoor jogging / Cooking a new recipe / Deep cleaning";
                break;
            case "EXCESS_TOTAL_USAGE":
                baseAdvice = "You're over your safe limit. Try breaking up screen time with movement.";
                alternateActivity = "Yoga / Power nap / Puzzles or Board games";
                break;
            case "PRODUCTIVE_USAGE":
                baseAdvice = "Excellent productivity! Just remember to take physical breaks for your eyes and back.";
                alternateActivity = "Stretching / Hydration break / Quick house chores";
                break;
            default:
                baseAdvice = "Good job staying within healthy limits. Maintain this consistency!";
                alternateActivity = "Night walk / Listening to a podcast / Gardening";
        }

        // Emotion-based tailoring
        String emotionalTailoring = "";
        if (emotion.contains("stressed") || emotion.contains("anxious")) {
            emotionalTailoring = "\n\nEmotion focus: Since you're feeling " + emotion + ", avoid doom-scrolling. ";
            alternateActivity = "Box breathing (4-4-4-4) / Nature walk";
        } else if (emotion.contains("tired") || emotion.contains("bored")) {
            emotionalTailoring = "\n\nEmotion focus: Feeling " + emotion + "? Screen time might feel like an easy escape, but a quick walk is better.";
            alternateActivity = "5-minute cold shower / Organizing your desk";
        } else if (emotion.contains("happy") || emotion.contains("productive")) {
            emotionalTailoring = "\n\nEmotion focus: Great to see you're " + emotion + "! Use this energy for off-screen creativity.";
        }

        record.setAlternateActivity(alternateActivity);
        return baseAdvice + emotionalTailoring + "\n\n🎯 Recommended Activity: " + alternateActivity;
    }

    private int percent(int part, int total) {
        if (total == 0) return 0;
        return (int) Math.round((part * 100.0) / total);
    }
}
